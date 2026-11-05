const test = require('node:test');
const assert = require('node:assert/strict');
const { spawn } = require('node:child_process');
const net = require('node:net');
const os = require('node:os');
const path = require('node:path');
const fs = require('node:fs/promises');

async function getFreePort() {
  const server = net.createServer();
  await new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(0, '127.0.0.1', resolve);
  });
  const port = server.address().port;
  await new Promise(resolve => server.close(resolve));
  return port;
}

async function waitForHealth(baseUrl, child) {
  for (let attempt = 0; attempt < 60; attempt += 1) {
    if (child.exitCode !== null) throw new Error('La API terminó antes de iniciar.');
    try {
      const response = await fetch(`${baseUrl}/health`);
      if (response.ok) return;
    } catch { /* El servidor puede tardar un momento en iniciar. */ }
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('La API no respondió dentro del tiempo esperado.');
}

test('API REST: salud, mediciones idempotentes y ciclo de acciones', async () => {
  const port = await getFreePort();
  const temporaryDataDir = await fs.mkdtemp(path.join(os.tmpdir(), 'clorocontrol-api-'));
  const baseUrl = `http://127.0.0.1:${port}/api`;
  const child = spawn(process.execPath, [path.join(__dirname, '..', 'server.js')], {
    env: { ...process.env, PORT: String(port), DATA_DIR: temporaryDataDir },
    stdio: ['ignore', 'pipe', 'pipe']
  });
  let childOutput = '';
  child.stdout.on('data', chunk => { childOutput += chunk.toString(); });
  child.stderr.on('data', chunk => { childOutput += chunk.toString(); });

  try {
    await waitForHealth(baseUrl, child);

    const health = await fetch(`${baseUrl}/health`).then(response => response.json());
    assert.equal(health.status, 'ok');

    const initialMeasurements = await fetch(`${baseUrl}/measurements`).then(response => response.json());
    assert.equal(initialMeasurements.length, 12);

    const measurement = {
      clientId: 'measurement-test-001',
      farm: 'Granja Norte',
      shed: 'Galpón 1',
      pointCode: 'N-01',
      location: 'Bebedero norte',
      chlorinePpm: 2.8,
      measuredAt: Date.now(),
      status: 'ADVERTENCIA',
      createdBy: 'operario@demo.cl'
    };

    const firstPost = await fetch(`${baseUrl}/measurements`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(measurement)
    });
    assert.equal(firstPost.status, 201);

    const repeatedPost = await fetch(`${baseUrl}/measurements`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(measurement)
    });
    assert.equal(repeatedPost.status, 200);

    const measurements = await fetch(`${baseUrl}/measurements`).then(response => response.json());
    assert.equal(measurements.length, 13);
    assert.equal(measurements.filter(item => item.clientId === measurement.clientId).length, 1);

    const action = {
      clientId: 'action-test-001',
      measurementClientId: measurement.clientId,
      verificationMeasurementClientId: 'verification-test-001',
      description: 'Se revisó el dosificador y se repitió la medición.',
      responsible: 'operario@demo.cl',
      createdAt: Date.now(),
      verifiedPpm: 3.5,
      evidenceSource: 'Galería',
      evidenceAvailable: true,
      status: 'PENDIENTE_VALIDACION',
      validatedBy: null
    };

    const actionPost = await fetch(`${baseUrl}/actions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(action)
    });
    assert.equal(actionPost.status, 201);

    const updatedAction = await fetch(`${baseUrl}/actions/${encodeURIComponent(action.clientId)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'VALIDADA', validatedBy: 'supervisor@demo.cl' })
    });
    assert.equal(updatedAction.status, 200);
    const updatedPayload = await updatedAction.json();
    assert.equal(updatedPayload.status, 'VALIDADA');

    const actions = await fetch(`${baseUrl}/actions`).then(response => response.json());
    assert.equal(actions.length, 1);
    assert.equal(actions[0].validatedBy, 'supervisor@demo.cl');
  } catch (error) {
    error.message += `\nSalida del servidor: ${childOutput}`;
    throw error;
  } finally {
    child.kill('SIGTERM');
    await new Promise(resolve => {
      if (child.exitCode !== null) return resolve();
      child.once('exit', resolve);
      setTimeout(resolve, 1000).unref();
    });
    await fs.rm(temporaryDataDir, { recursive: true, force: true });
  }
});
