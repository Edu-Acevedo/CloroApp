/**
 * API REST local de CloroControl para el MVP académico.
 * Requiere Node.js 18 o superior. No utiliza dependencias externas.
 * Solo debe ejecutarse con datos ficticios en una red de desarrollo.
 */
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');

const PORT = Number(process.env.PORT || 6767);
const DATA_DIR = process.env.DATA_DIR || path.join(__dirname, 'data');
const DATA_FILE = path.join(DATA_DIR, 'clorocontrol.json');

function initialMeasurements() {
  const now = Date.now();
  const samples = [
    ['Granja Norte', 'Galpón 1', 'N-01', 'Bebedero norte', 3.72, 4],
    ['Granja Norte', 'Galpón 1', 'N-02', 'Línea central', 2.65, 8],
    ['Granja Norte', 'Galpón 2', 'N-03', 'Bebedero sur', 4.21, 12],
    ['Granja Norte', 'Galpón 2', 'N-04', 'Extremo de línea', 1.72, 16],
    ['Granja Central', 'Galpón 1', 'C-01', 'Bebedero norte', 5.35, 20],
    ['Granja Central', 'Galpón 1', 'C-02', 'Línea central', 3.91, 24],
    ['Granja Central', 'Galpón 2', 'C-03', 'Bebedero sur', 4.08, 28],
    ['Granja Central', 'Galpón 2', 'C-04', 'Extremo de línea', 6.24, 32],
    ['Granja Sur', 'Galpón 1', 'S-01', 'Bebedero norte', 3.44, 36],
    ['Granja Sur', 'Galpón 1', 'S-02', 'Línea central', 2.12, 40],
    ['Granja Sur', 'Galpón 2', 'S-03', 'Bebedero sur', 4.66, 44],
    ['Granja Sur', 'Galpón 2', 'S-04', 'Extremo de línea', 5.00, 48]
  ];

  return samples.map(([farm, shed, pointCode, location, chlorinePpm, ageMinutes]) => ({
    clientId: `demo-seed-${pointCode}`,
    farm,
    shed,
    pointCode,
    location,
    chlorinePpm,
    measuredAt: now - ageMinutes * 60_000,
    status: chlorinePpm < 2 || chlorinePpm > 6
      ? 'CRITICO'
      : chlorinePpm < 3 || chlorinePpm > 5 ? 'ADVERTENCIA' : 'NORMAL',
    createdBy: 'datos.demo@clorocontrol.cl',
    receivedAt: now
  }));
}

function loadData() {
  fs.mkdirSync(DATA_DIR, { recursive: true });
  if (!fs.existsSync(DATA_FILE)) {
    const initial = { measurements: initialMeasurements(), actions: [] };
    fs.writeFileSync(DATA_FILE, JSON.stringify(initial, null, 2));
    return initial;
  }
  try {
    const parsed = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
    const normalized = {
      measurements: Array.isArray(parsed.measurements) ? parsed.measurements : [],
      actions: Array.isArray(parsed.actions) ? parsed.actions : []
    };
    const ids = new Set(normalized.measurements.map(item => item.clientId));
    for (const sample of initialMeasurements()) {
      if (!ids.has(sample.clientId)) normalized.measurements.push(sample);
    }
    return normalized;
  } catch (error) {
    console.error('No se pudo leer la base JSON:', error.message);
    throw error;
  }
}

let data = loadData();

function persist() {
  const temporary = `${DATA_FILE}.tmp`;
  fs.writeFileSync(temporary, JSON.stringify(data, null, 2));
  fs.renameSync(temporary, DATA_FILE);
}

function sendJson(res, statusCode, value) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Accept'
  });
  res.end(JSON.stringify(value));
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let raw = '';
    req.on('data', chunk => {
      raw += chunk;
      if (raw.length > 1024 * 1024) {
        reject(new Error('El cuerpo de la solicitud supera 1 MB.'));
        req.destroy();
      }
    });
    req.on('end', () => {
      try { resolve(raw ? JSON.parse(raw) : {}); }
      catch { reject(new Error('JSON inválido.')); }
    });
    req.on('error', reject);
  });
}

function validMeasurement(body) {
  return typeof body.clientId === 'string' && body.clientId.length > 0 &&
    typeof body.farm === 'string' && typeof body.shed === 'string' &&
    typeof body.pointCode === 'string' && typeof body.location === 'string' &&
    Number.isFinite(Number(body.chlorinePpm)) &&
    Number(body.chlorinePpm) >= 0 &&
    Number.isFinite(Number(body.measuredAt));
}

function validAction(body) {
  return typeof body.clientId === 'string' && body.clientId.length > 0 &&
    typeof body.measurementClientId === 'string' && body.measurementClientId.length > 0 &&
    typeof body.description === 'string' && body.description.trim().length >= 8 &&
    body.description.trim().length <= 500 &&
    Number.isFinite(Number(body.verifiedPpm)) &&
    Number(body.verifiedPpm) >= 0;
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') return sendJson(res, 204, {});
  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const route = url.pathname.replace(/\/+$/, '');

  if (req.method === 'GET' && route === '/api/health') {
    return sendJson(res, 200, { status: 'ok', service: 'CloroControl API', version: '1.0.0' });
  }

  if (req.method === 'GET' && route === '/api/measurements') {
    return sendJson(res, 200, data.measurements);
  }

  if (req.method === 'POST' && route === '/api/measurements') {
    try {
      const body = await readBody(req);
      if (!validMeasurement(body)) return sendJson(res, 400, { error: 'Faltan campos válidos de medición.' });
      const existing = data.measurements.find(item => item.clientId === body.clientId);
      if (existing) return sendJson(res, 200, existing);
      const saved = { ...body, id: crypto.randomUUID(), receivedAt: Date.now() };
      data.measurements.push(saved);
      persist();
      return sendJson(res, 201, saved);
    } catch (error) {
      return sendJson(res, 400, { error: error.message });
    }
  }

  if (req.method === 'GET' && route === '/api/actions') {
    return sendJson(res, 200, data.actions);
  }

  if (req.method === 'POST' && route === '/api/actions') {
    try {
      const body = await readBody(req);
      if (!validAction(body)) return sendJson(res, 400, { error: 'Faltan campos válidos de acción correctiva.' });
      const existing = data.actions.find(item => item.clientId === body.clientId);
      if (existing) return sendJson(res, 200, existing);
      const saved = { ...body, id: crypto.randomUUID(), receivedAt: Date.now() };
      data.actions.push(saved);
      persist();
      return sendJson(res, 201, saved);
    } catch (error) {
      return sendJson(res, 400, { error: error.message });
    }
  }

  if (req.method === 'PUT' && route.startsWith('/api/actions/')) {
    try {
      const clientId = decodeURIComponent(route.substring('/api/actions/'.length));
      const body = await readBody(req);
      const index = data.actions.findIndex(item => item.clientId === clientId);
      if (index < 0) return sendJson(res, 404, { error: 'Acción no encontrada.' });
      data.actions[index] = { ...data.actions[index], ...body, clientId, updatedAt: Date.now() };
      persist();
      return sendJson(res, 200, data.actions[index]);
    } catch (error) {
      return sendJson(res, 400, { error: error.message });
    }
  }

  return sendJson(res, 404, { error: 'Ruta no encontrada.' });
});

if (require.main === module) {
  server.listen(PORT, '127.0.0.1', () => {
    const address = server.address();
    const activePort = typeof address === 'object' && address ? address.port : PORT;
    console.log(`CloroControl API disponible en http://localhost:${activePort}/api/health`);
    console.log(`Para emulador Android: usa http://10.0.2.2:${activePort}/api/`);
    console.log(`Los datos se guardan en ${DATA_FILE}`);
  });
}

module.exports = { server, validMeasurement, validAction };
