import express from 'express';
import { createServer as createViteServer } from 'vite';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { GoogleGenAI } from '@google/genai';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function startServer() {
  const app = express();
  app.use(express.json());

  const PORT = Number(process.env.PORT || 3000);

  // Initialize Gemini AI if key is available
  const apiKey = process.env.GEMINI_API_KEY;
  const ai = apiKey ? new GoogleGenAI({ apiKey }) : null;

  // API Routes
  app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', studio: 'Personal AI Creative Studio', owner: 'Siavash Ali-Miri', status_flag: 'PARKED' });
  });

  app.post('/api/ai/generate', async (req, res) => {
    try {
      const { prompt, moduleName } = req.body;
      if (!ai) {
        return res.status(400).json({ 
          error: 'Gemini API key not configured. Please add GEMINI_API_KEY in secrets.' 
        });
      }

      const systemInstruction = `You are the AI Assistant for the Personal AI Creative Studio (Owner: Siavash Ali-Miri). 
      This studio is designed for fictional character creation, virtual clothing try-on, fashion visualization, wedding/formal wear, and scene production. 
      The project is currently PARKED (Development Priority: NONE — Future Project). 
      Provide detailed, creative, professional concepts, prompt engineering specs, or technical feasibility evaluations tailored to module: ${moduleName || 'General'}.`;

      const response = await ai.models.generateContent({
        model: 'gemini-2.5-flash',
        contents: prompt,
        config: {
          systemInstruction,
          temperature: 0.7,
        }
      });

      res.json({ success: true, text: response.text });
    } catch (err: any) {
      console.error('Gemini API Error:', err);
      res.status(500).json({ success: false, error: err.message || 'AI generation failed' });
    }
  });

  // Setup Vite middleware for development
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.join(__dirname, 'dist')));
    app.get('*', (req, res) => {
      res.sendFile(path.join(__dirname, 'dist', 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Personal AI Creative Studio running on http://localhost:${PORT}`);
  });
}

startServer().catch(err => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
