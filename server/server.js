/**
 * Web2APK Express Backend API Server
 */
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
const BuildWorker = require('./builder/worker');

const app = express();
const PORT = process.env.PORT || 3000;

// Security Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Rate Limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: { error: 'Too many requests from this IP, please try again later.' }
});
app.use('/api/', limiter);

// Storage & Upload Setup
const uploadDir = path.join(__dirname, 'uploads');
const artifactsDir = path.join(__dirname, 'artifacts');
fs.mkdirSync(uploadDir, { recursive: true });
fs.mkdirSync(artifactsDir, { recursive: true });

const upload = multer({
  dest: uploadDir,
  limits: { fileSize: 50 * 1024 * 1024 } // 50MB max upload
});

const worker = new BuildWorker({ artifactsDir });

// In-memory job store (backed by SQLite/PostgreSQL in full deployment)
const projectsDb = new Map();
const buildsDb = new Map();

// Routes

// 1. Health Check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    workers: 'online',
    activeBuilds: Array.from(buildsDb.values()).filter(b => b.status === 'BUILDING').length
  });
});

// 2. Create / Update Project
app.post('/api/projects', (req, res) => {
  const project = req.body;
  if (!project.name || !project.packageName) {
    return res.status(400).json({ error: 'App name and package name are required.' });
  }
  const id = project.id || uuidv4();
  const saved = { ...project, id, updatedAt: Date.now() };
  projectsDb.set(id, saved);
  res.json(saved);
});

// 3. List Projects
app.get('/api/projects', (req, res) => {
  res.json(Array.from(projectsDb.values()));
});

// 4. Trigger Build
app.post('/api/builds', upload.single('zipFile'), async (req, res) => {
  try {
    const rawProject = req.body.project ? JSON.parse(req.body.project) : req.body;
    const rightsConfirmed = req.body.rightsConfirmed === true || req.body.rightsConfirmed === 'true';

    // Mandatory user rights verification
    if (!rightsConfirmed) {
      return res.status(400).json({
        error: 'Compliance Violation: You must confirm that you have the necessary rights or permission to package and distribute this content.'
      });
    }

    const buildId = 'BUILD-' + uuidv4().substring(0, 8).toUpperCase();
    const buildRecord = {
      id: buildId,
      projectId: rawProject.id || uuidv4(),
      project: rawProject,
      status: 'QUEUED',
      buildType: req.body.buildType || 'DEBUG',
      logs: [],
      startedAt: Date.now()
    };
    buildsDb.set(buildId, buildRecord);

    // Asynchronously process with worker
    setTimeout(async () => {
      buildRecord.status = 'BUILDING';
      try {
        const result = await worker.processJob({
          id: buildId,
          project: rawProject,
          buildType: buildRecord.buildType,
          zipPath: req.file ? req.file.path : null
        }, (logLine) => {
          buildRecord.logs.push(logLine);
        });

        buildRecord.status = result.status;
        buildRecord.apkUrl = result.apkUrl;
        buildRecord.apkPath = result.apkPath;
        buildRecord.apkSizeMb = result.apkSizeMb;
        buildRecord.sha256 = result.sha256;
        buildRecord.completedAt = Date.now();
      } catch (err) {
        buildRecord.status = 'FAILED';
        buildRecord.error = err.message;
        buildRecord.logs.push(`[ERROR] Build failed: ${err.message}`);
      }
    }, 100);

    res.json({
      buildId,
      status: 'QUEUED',
      message: 'Build job successfully enqueued'
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 5. Build Status
app.get('/api/builds/:id/status', (req, res) => {
  const build = buildsDb.get(req.params.id);
  if (!build) return res.status(404).json({ error: 'Build not found' });
  res.json(build);
});

// 6. Download APK
app.get('/api/builds/:id/download', (req, res) => {
  const build = buildsDb.get(req.params.id);
  if (!build || !build.apkPath || !fs.existsSync(build.apkPath)) {
    return res.status(404).json({ error: 'APK file not available or build incomplete' });
  }
  res.download(build.apkPath);
});

// 7. Admin Metrics
app.get('/api/admin/metrics', (req, res) => {
  res.json({
    totalProjects: projectsDb.size,
    totalBuilds: buildsDb.size,
    activeWorkers: 4,
    queueLength: 0,
    serverUptimeSeconds: process.uptime()
  });
});

app.listen(PORT, () => {
  console.log(`Web2APK Backend API listening on port ${PORT}`);
});
