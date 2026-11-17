/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Sparkles, 
  ShieldAlert, 
  Layers, 
  UserCheck, 
  Shirt, 
  HeartHandshake, 
  Crown, 
  Film, 
  Cpu, 
  CheckCircle2, 
  Lock, 
  FileText, 
  Send, 
  Terminal, 
  Download, 
  RefreshCw,
  Sliders,
  Eye,
  Settings,
  HelpCircle,
  CheckSquare,
  Bookmark
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState<'charter' | 'module-a' | 'module-b' | 'module-c' | 'module-d' | 'module-e' | 'module-f' | 'tech' | 'ai-assistant'>('charter');

  // Interactive state for Studio Tools & Mockups
  const [charName, setCharName] = useState('Elena Vance');
  const [charAge, setCharAge] = useState(28);
  const [charStyle, setCharStyle] = useState('Cinematic Editorial');
  const [charCategory, setCharCategory] = useState('Evening Wear');
  const [tryOnModel, setTryOnModel] = useState('Model #04 (Studio Fit)');
  const [tryOnClothing, setTryOnClothing] = useState('Silk Satin Slip Dress - Emerald Green with Lace Trim');
  const [sceneType, setSceneType] = useState('Wedding Reception Grand Entrance');
  const [lightingSetup, setLightingSetup] = useState('Warm Golden Hour & Soft Rim Light');

  // AI Assistant State
  const [aiPrompt, setAiPrompt] = useState('');
  const [aiModuleSelection, setAiModuleSelection] = useState('Module A — Fictional Character Studio');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiResult, setAiResult] = useState<string | null>(null);
  const [aiError, setAiError] = useState<string | null>(null);

  const handleGenerateAI = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!aiPrompt.trim()) return;
    setAiLoading(true);
    setAiError(null);
    try {
      const res = await fetch('/api/ai/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: aiPrompt, moduleName: aiModuleSelection })
      });
      const data = await res.json();
      if (data.success) {
        setAiResult(data.text);
      } else {
        setAiError(data.error || 'Failed to generate AI response');
      }
    } catch (err: any) {
      setAiError(err.message || 'Network error connecting to Gemini AI server');
    } finally {
      setAiLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-rose-500 selection:text-white">
      {/* Top Banner: Parked Status & Owner Info */}
      <div className="bg-gradient-to-r from-rose-950 via-purple-950 to-slate-950 border-b border-rose-900/40 px-4 py-3 shadow-lg">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-rose-500/20 border border-rose-500/40 flex items-center justify-center text-rose-400">
              <Sparkles className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-bold tracking-wider text-rose-200 text-sm md:text-base">PERSONAL AI CREATIVE STUDIO</h1>
                <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-rose-500/30 text-rose-300 border border-rose-500/50 flex items-center gap-1">
                  <Lock className="w-3 h-3" /> PARKED
                </span>
              </div>
              <p className="text-xs text-rose-300/80">Owner: Siavash Ali-Miri | Development Priority: NONE (Future Project)</p>
            </div>
          </div>
          <div className="flex items-center gap-2 text-xs text-slate-300 bg-slate-900/80 px-3 py-1.5 rounded-lg border border-slate-800">
            <ShieldAlert className="w-4 h-4 text-amber-400" />
            <span>Final Rule: Preserve the idea. Do not start active development until explicitly authorized.</span>
          </div>
        </div>
      </div>

      {/* Navigation Header */}
      <header className="bg-slate-900/90 backdrop-blur border-b border-slate-800 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 flex items-center justify-between overflow-x-auto py-2 no-scrollbar">
          <nav className="flex items-center gap-1 min-w-max">
            <button
              onClick={() => setActiveTab('charter')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'charter' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <FileText className="w-4 h-4" /> Charter
            </button>
            <button
              onClick={() => setActiveTab('module-a')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-a' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <UserCheck className="w-4 h-4" /> A. Character Studio
            </button>
            <button
              onClick={() => setActiveTab('module-b')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-b' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Shirt className="w-4 h-4" /> B. Virtual Try-On
            </button>
            <button
              onClick={() => setActiveTab('module-c')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-c' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Crown className="w-4 h-4" /> C. Fashion & Lingerie
            </button>
            <button
              onClick={() => setActiveTab('module-d')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-d' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <HeartHandshake className="w-4 h-4" /> D. Wedding & Formal
            </button>
            <button
              onClick={() => setActiveTab('module-e')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-e' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Film className="w-4 h-4" /> E. Scene & Story
            </button>
            <button
              onClick={() => setActiveTab('module-f')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'module-f' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Layers className="w-4 h-4" /> F. Image & Video
            </button>
            <button
              onClick={() => setActiveTab('tech')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'tech' ? 'bg-rose-600 text-white shadow-lg shadow-rose-900/40' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Cpu className="w-4 h-4" /> Tech & Reality Check
            </button>
            <button
              onClick={() => setActiveTab('ai-assistant')}
              className={`px-3 py-2 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${activeTab === 'ai-assistant' ? 'bg-purple-600 text-white shadow-lg shadow-purple-900/40' : 'text-purple-300 hover:text-purple-100 hover:bg-purple-950/50'}`}
            >
              <Sparkles className="w-4 h-4 text-amber-300" /> Gemini Studio AI
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6">
        {/* 1. PROJECT CHARTER TAB */}
        {activeTab === 'charter' && (
          <div className="space-y-6">
            <div className="bg-gradient-to-br from-slate-900 to-slate-950 border border-slate-800 rounded-2xl p-6 shadow-xl relative overflow-hidden">
              <div className="absolute top-0 right-0 w-96 h-96 bg-rose-500/5 rounded-full blur-3xl pointer-events-none"></div>
              <div className="flex items-center justify-between flex-wrap gap-4 mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-rose-400 font-semibold">Project Initialization Script v1.0</span>
                  <h2 className="text-2xl md:text-3xl font-bold text-white mt-1">Personal AI Creative Studio</h2>
                </div>
                <div className="flex items-center gap-2">
                  <span className="px-3 py-1 bg-amber-500/20 text-amber-300 border border-amber-500/40 rounded-full text-xs font-semibold">
                    Status: PARKED
                  </span>
                  <span className="px-3 py-1 bg-slate-800 text-slate-300 border border-slate-700 rounded-full text-xs font-semibold">
                    Priority: NONE
                  </span>
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-6 text-sm text-slate-300">
                <div className="bg-slate-900/60 p-5 rounded-xl border border-slate-800/80 space-y-3">
                  <h3 className="text-rose-400 font-semibold text-base flex items-center gap-2">
                    <FileText className="w-4 h-4" /> 1. Project Purpose
                  </h3>
                  <p className="leading-relaxed text-slate-300">
                    Create a personal AI-powered creative studio for fictional character creation, virtual clothing try-on, fashion visualization, image editing, and creative scene production.
                  </p>
                  <p className="text-xs text-slate-400 italic">
                    Reserved for future personal use, experimentation, and entertainment. NOT intended for commercial release, public distribution, SaaS operation, or monetization.
                  </p>
                </div>

                <div className="bg-slate-900/60 p-5 rounded-xl border border-slate-800/80 space-y-3">
                  <h3 className="text-rose-400 font-semibold text-base flex items-center gap-2">
                    <ShieldAlert className="w-4 h-4" /> 3. Security & Privacy Principles
                  </h3>
                  <ul className="space-y-1.5 text-xs text-slate-300">
                    <li className="flex items-center gap-2"><CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" /> Local-first architecture whenever practical</li>
                    <li className="flex items-center gap-2"><CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" /> No automatic uploading of personal images</li>
                    <li className="flex items-center gap-2"><CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" /> No mandatory paid subscriptions or API key exposure</li>
                    <li className="flex items-center gap-2"><CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" /> Adult-themed fictional characters must be clearly fictional adults</li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Core Modules Overview Grid */}
            <div>
              <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <Layers className="w-5 h-5 text-rose-500" /> Core Studio Modules Roadmap
              </h3>
              <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {[
                  { id: 'module-a', title: 'Module A: Fictional Character Studio', desc: 'Create fully fictional adult characters, define appearance, select outfits, poses, backgrounds, and visual styles.', icon: UserCheck, color: 'text-rose-400' },
                  { id: 'module-b', title: 'Module B: Virtual Clothing Try-On', desc: 'Input clothing product image & model image. Virtually display clothing while preserving colors, lace, textures.', icon: Shirt, color: 'text-purple-400' },
                  { id: 'module-c', title: 'Module C: Fashion & Lingerie Studio', desc: 'Presentation of lingerie, swimwear, and costumes. Experimental product catalog and promotional concepts.', icon: Crown, color: 'text-pink-400' },
                  { id: 'module-d', title: 'Module D: Wedding & Formal Wear', desc: 'Virtual wedding dress & groom suit fitting. Formal clothing visualization and bride/groom co-display.', icon: HeartHandshake, color: 'text-amber-400' },
                  { id: 'module-e', title: 'Module E: Scene & Story Studio', desc: 'Create visual scenes: wedding hall entrance, reception, dancing, wedding car, and creative fictional settings.', icon: Film, color: 'text-blue-400' },
                  { id: 'module-f', title: 'Module F: Basic Image & Video Tools', desc: 'Image management, collections, basic editing, subtitle overlays, and short video creation feasibility.', icon: Layers, color: 'text-emerald-400' },
                ].map((mod) => {
                  const Icon = mod.icon;
                  return (
                    <div 
                      key={mod.id} 
                      onClick={() => setActiveTab(mod.id as any)}
                      className="bg-slate-900/80 border border-slate-800 hover:border-rose-500/50 rounded-xl p-5 cursor-pointer transition flex flex-col justify-between group shadow-lg"
                    >
                      <div>
                        <div className="flex items-center justify-between mb-3">
                          <div className={`w-9 h-9 rounded-lg bg-slate-800 flex items-center justify-center ${mod.color}`}>
                            <Icon className="w-5 h-5" />
                          </div>
                          <span className="text-xs text-slate-500 group-hover:text-rose-400 transition">Explore &rarr;</span>
                        </div>
                        <h4 className="font-semibold text-white text-sm mb-1">{mod.title}</h4>
                        <p className="text-xs text-slate-400 line-clamp-3">{mod.desc}</p>
                      </div>
                      <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-500">
                        <span>Status: Parked</span>
                        <span className="text-rose-400 font-medium">Concept Ready</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Current Priorities Notice */}
            <div className="bg-slate-900/60 border border-amber-500/30 rounded-xl p-5">
              <h4 className="text-amber-300 font-semibold text-sm mb-2 flex items-center gap-2">
                <ShieldAlert className="w-4 h-4" /> 7. Project Priorities & Execution Order
              </h4>
              <p className="text-xs text-slate-300 mb-3">
                This project must not interrupt or delay current higher-priority milestones:
              </p>
              <ol className="list-decimal list-inside text-xs text-slate-400 space-y-1">
                <li>Complete existing unfinished applications.</li>
                <li>Finish AFRINA Ad Studio.</li>
                <li>Audit FaceGuard / Roozegar functionality.</li>
                <li>Audit and validate SalonYar and other existing applications.</li>
                <li>Resolve FarshBazaar release packaging.</li>
                <li>Verify APK/AAB generation, signing, installation, and release readiness.</li>
              </ol>
            </div>
          </div>
        )}

        {/* 2. MODULE A: CHARACTER STUDIO */}
        {activeTab === 'module-a' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-rose-400 font-semibold">Module A</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Fictional Character Studio</h2>
                </div>
                <span className="px-3 py-1 bg-rose-500/20 text-rose-300 rounded-full text-xs font-medium">Interactive Studio Mockup</span>
              </div>

              <div className="grid md:grid-cols-3 gap-6">
                {/* Character Config Panel */}
                <div className="space-y-4 bg-slate-950 p-4 rounded-xl border border-slate-800">
                  <h3 className="text-sm font-semibold text-rose-300 flex items-center gap-2">
                    <Sliders className="w-4 h-4" /> Character Parameters
                  </h3>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Character Name / Identifier</label>
                    <input 
                      type="text" 
                      value={charName} 
                      onChange={(e) => setCharName(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-rose-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Age (Fictional Adult)</label>
                    <input 
                      type="number" 
                      value={charAge} 
                      onChange={(e) => setCharAge(Number(e.target.value))}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-rose-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Visual Art Style</label>
                    <select 
                      value={charStyle} 
                      onChange={(e) => setCharStyle(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-rose-500"
                    >
                      <option>Cinematic Editorial</option>
                      <option>High Fashion Magazine</option>
                      <option>Studio Portrait Soft Lighting</option>
                      <option>Dramatic Noir</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Clothing Category</label>
                    <select 
                      value={charCategory} 
                      onChange={(e) => setCharCategory(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-rose-500"
                    >
                      <option>Casual Wear</option>
                      <option>Formal & Evening Wear</option>
                      <option>Swimwear & Resort</option>
                      <option>Lingerie & Intimates</option>
                    </select>
                  </div>

                  <button 
                    onClick={() => {
                      setAiPrompt(`Generate detailed character prompt specifications for fictional adult character ${charName}, age ${charAge}, style ${charStyle}, category ${charCategory}, including facial features, hair color, eye color, expression, pose, lighting, and wardrobe details.`);
                      setActiveTab('ai-assistant');
                    }}
                    className="w-full py-2.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-xs font-medium transition flex items-center justify-center gap-2 shadow-lg shadow-rose-900/30"
                  >
                    <Sparkles className="w-4 h-4" /> Generate Character Prompt with Gemini
                  </button>
                </div>

                {/* Character Preview Canvas */}
                <div className="md:col-span-2 bg-slate-950 p-6 rounded-xl border border-slate-800 flex flex-col justify-between items-center text-center relative overflow-hidden">
                  <div className="absolute inset-0 bg-gradient-to-t from-rose-950/20 via-transparent to-transparent pointer-events-none"></div>
                  
                  <div className="w-full flex items-center justify-between mb-4 z-10">
                    <span className="text-xs text-slate-400">Preview Canvas</span>
                    <span className="text-xs bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/30">Stable Seed Ready</span>
                  </div>

                  <div className="my-auto space-y-3 z-10">
                    <div className="w-24 h-24 mx-auto rounded-full bg-rose-900/30 border border-rose-500/40 flex items-center justify-center text-rose-300 text-2xl font-bold shadow-xl">
                      {charName.split(' ').map(n => n[0]).join('')}
                    </div>
                    <h3 className="text-lg font-bold text-white">{charName}</h3>
                    <p className="text-xs text-slate-400 max-w-sm mx-auto">
                      Fictional adult character profile configured for <span className="text-rose-300">{charCategory}</span> under <span className="text-rose-300">{charStyle}</span>. Identity consistency parameters locked.
                    </p>
                  </div>

                  <div className="w-full mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-500 z-10">
                    <span>Age: {charAge}</span>
                    <span>Style: {charStyle}</span>
                    <span className="text-rose-400">Ready for Pipeline</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 3. MODULE B: VIRTUAL CLOTHING TRY-ON */}
        {activeTab === 'module-b' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-purple-400 font-semibold">Module B</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Virtual Clothing Try-On Studio</h2>
                </div>
                <span className="px-3 py-1 bg-purple-500/20 text-purple-300 rounded-full text-xs font-medium">Texture & Fit Preservation</span>
              </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4 bg-slate-950 p-5 rounded-xl border border-slate-800">
                  <h3 className="text-sm font-semibold text-purple-300 flex items-center gap-2">
                    <Shirt className="w-4 h-4" /> Try-On Configuration
                  </h3>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Target Model / Character Base</label>
                    <select 
                      value={tryOnModel} 
                      onChange={(e) => setTryOnModel(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-purple-500"
                    >
                      <option>Model #04 (Studio Fit)</option>
                      <option>Model #09 (Editorial Pose)</option>
                      <option>Elena Vance (Character A)</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Clothing Product Description / Image URL</label>
                    <input 
                      type="text" 
                      value={tryOnClothing} 
                      onChange={(e) => setTryOnClothing(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-purple-500"
                    />
                  </div>

                  <div className="p-3 bg-slate-900 rounded-lg border border-slate-800 text-xs space-y-2 text-slate-300">
                    <p className="font-semibold text-purple-300">Accuracy & Detail Preservation Rules:</p>
                    <ul className="list-disc list-inside space-y-1 text-slate-400">
                      <li>Preserve product colors, cuts, straps, and stitching.</li>
                      <li>Maintain lace patterns, sheer textures, and fabric drape.</li>
                      <li>Avoid distortion of garment logos and hardware details.</li>
                    </ul>
                  </div>

                  <button 
                    onClick={() => {
                      setActiveTab('ai-assistant');
                      setAiPrompt(`Analyze virtual try-on workflow for placing "${tryOnClothing}" onto "${tryOnModel}", detailing ControlNet depth/pose guidelines and texture preservation strategies.`);
                    }}
                    className="w-full py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-medium transition flex items-center justify-center gap-2 shadow-lg shadow-purple-900/30"
                  >
                    <Sparkles className="w-4 h-4" /> Evaluate Try-On Feasibility with Gemini
                  </button>
                </div>

                <div className="bg-slate-950 p-6 rounded-xl border border-slate-800 flex flex-col justify-center items-center text-center">
                  <div className="w-16 h-16 rounded-2xl bg-purple-950/40 border border-purple-500/30 flex items-center justify-center text-purple-400 mb-4">
                    <Shirt className="w-8 h-8" />
                  </div>
                  <h4 className="font-bold text-white text-base mb-1">Virtual Try-On Pipeline Mockup</h4>
                  <p className="text-xs text-slate-400 max-w-sm mb-4">
                    Combining model pose estimation with garment warping algorithms to virtually display <span className="text-purple-300">{tryOnClothing}</span> on <span className="text-purple-300">{tryOnModel}</span>.
                  </p>
                  <div className="inline-flex items-center gap-2 px-3 py-1 bg-slate-900 rounded-full text-xs text-slate-400 border border-slate-800">
                    <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                    <span>Status: Planned for Future Phase</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 4. MODULE C: FASHION & LINGERIE STUDIO */}
        {activeTab === 'module-c' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-pink-400 font-semibold">Module C</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Fashion & Lingerie Studio</h2>
                </div>
                <span className="px-3 py-1 bg-pink-500/20 text-pink-300 rounded-full text-xs font-medium">Experimental Catalog Concepts</span>
              </div>

              <div className="grid md:grid-cols-3 gap-4">
                {[
                  { title: 'Lingerie & Underwear Sets', desc: 'Virtual presentation of delicate lingerie sets, lace bras, and intimate apparel with high-fidelity fabric rendering.' },
                  { title: 'Swimwear & Resort Collections', desc: 'Poolside and beach lighting simulations for swimwear catalogs and promotional concepts.' },
                  { title: 'Product-Cover Concepts', desc: 'High-end promotional image concepts using fictional adult models for digital portfolios.' }
                ].map((item, idx) => (
                  <div key={idx} className="bg-slate-950 p-5 rounded-xl border border-slate-800 flex flex-col justify-between">
                    <div>
                      <h3 className="font-semibold text-white text-sm mb-2 text-pink-300">{item.title}</h3>
                      <p className="text-xs text-slate-400 leading-relaxed">{item.desc}</p>
                    </div>
                    <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
                      <span className="text-slate-500">Module C Spec</span>
                      <button 
                        onClick={() => {
                          setAiPrompt(`Create promotional catalog concepts and lighting directives for ${item.title} within the Personal AI Creative Studio.`);
                          setActiveTab('ai-assistant');
                        }}
                        className="text-pink-400 hover:text-pink-300 flex items-center gap-1 font-medium"
                      >
                        Generate Ideas &rarr;
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 5. MODULE D: WEDDING & FORMAL WEAR */}
        {activeTab === 'module-d' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-amber-400 font-semibold">Module D</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Wedding & Formal Wear Studio</h2>
                </div>
                <span className="px-3 py-1 bg-amber-500/20 text-amber-300 rounded-full text-xs font-medium">Bridal & Groom Fitting Simulator</span>
              </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4 bg-slate-950 p-5 rounded-xl border border-slate-800">
                  <h3 className="text-sm font-semibold text-amber-300 flex items-center gap-2">
                    <HeartHandshake className="w-4 h-4" /> Wedding Fitting Configurator
                  </h3>
                  <div className="space-y-3 text-xs text-slate-300">
                    <div>
                      <label className="text-slate-400 block mb-1">Bridal Gown Style</label>
                      <select className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white">
                        <option>A-Line Lace Chantilly Gown</option>
                        <option>Ballgown with Cathedral Train</option>
                        <option>Sleek Minimalist Crepe Slip</option>
                      </select>
                    </div>
                    <div>
                      <label className="text-slate-400 block mb-1">Groom Suit Style</label>
                      <select className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white">
                        <option>Tailored Navy Tuxedo with Satin Lapel</option>
                        <option>Classic Black Three-Piece Suit</option>
                        <option>Modern Charcoal Slim Fit</option>
                      </select>
                    </div>
                  </div>
                  <button 
                    onClick={() => {
                      setAiPrompt(`Create a detailed co-display prompt for a bride in an A-Line Lace gown and groom in a Navy Tuxedo, staged in a luxurious botanical conservatory for wedding visualization.`);
                      setActiveTab('ai-assistant');
                    }}
                    className="w-full py-2.5 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-xs font-medium transition flex items-center justify-center gap-2 shadow-lg shadow-amber-900/30"
                  >
                    <Sparkles className="w-4 h-4" /> Generate Wedding Co-Display Prompt
                  </button>
                </div>

                <div className="bg-slate-950 p-6 rounded-xl border border-slate-800 flex flex-col justify-center items-center text-center">
                  <div className="w-16 h-16 rounded-2xl bg-amber-950/40 border border-amber-500/30 flex items-center justify-center text-amber-400 mb-4">
                    <Crown className="w-8 h-8" />
                  </div>
                  <h4 className="font-bold text-white text-base mb-1">Bride & Groom Co-Display</h4>
                  <p className="text-xs text-slate-400 max-w-sm mb-4">
                    Simulate simultaneous multi-character rendering with consistent facial features, lighting harmony, and formal wardrobe pairing.
                  </p>
                  <span className="text-xs text-amber-300 bg-amber-950/30 px-3 py-1 rounded-full border border-amber-500/30">
                    Status: Parked Feature Spec
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 6. MODULE E: SCENE & STORY STUDIO */}
        {activeTab === 'module-e' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-blue-400 font-semibold">Module E</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Scene & Story Studio</h2>
                </div>
                <span className="px-3 py-1 bg-blue-500/20 text-blue-300 rounded-full text-xs font-medium">Environment & Lighting Production</span>
              </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4 bg-slate-950 p-5 rounded-xl border border-slate-800">
                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Scene Concept</label>
                    <select 
                      value={sceneType} 
                      onChange={(e) => setSceneType(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white"
                    >
                      <option>Wedding Reception Grand Entrance</option>
                      <option>Romantic Couple Dancing Under Chandeliers</option>
                      <option>Couple Entering Vintage Wedding Car</option>
                      <option>Cinematic Fictional Sunset Terrace</option>
                    </select>
                  </div>

                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Lighting & Environment</label>
                    <select 
                      value={lightingSetup} 
                      onChange={(e) => setLightingSetup(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white"
                    >
                      <option>Warm Golden Hour & Soft Rim Light</option>
                      <option>Dramatic Ballroom Chandeliers & Bokeh</option>
                      <option>Moody Cinematic Noir Shadows</option>
                    </select>
                  </div>

                  <button 
                    onClick={() => {
                      setAiPrompt(`Write a cinematic scene production script and Stable Diffusion / Imagen prompt for: ${sceneType} with ${lightingSetup}. Include camera angle, character emotions, and background ambience.`);
                      setActiveTab('ai-assistant');
                    }}
                    className="w-full py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-xs font-medium transition flex items-center justify-center gap-2 shadow-lg shadow-blue-900/30"
                  >
                    <Sparkles className="w-4 h-4" /> Generate Scene Script with Gemini
                  </button>
                </div>

                <div className="bg-slate-950 p-6 rounded-xl border border-slate-800 flex flex-col justify-center items-center text-center">
                  <Film className="w-10 h-10 text-blue-400 mb-3" />
                  <h4 className="font-bold text-white text-sm mb-1">{sceneType}</h4>
                  <p className="text-xs text-slate-400 max-w-xs">
                    Configured with <span className="text-blue-300">{lightingSetup}</span>. Environment depth maps and volumetric lighting parameters ready for future rendering engines.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 7. MODULE F: IMAGE & VIDEO TOOLS */}
        {activeTab === 'module-f' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-emerald-400 font-semibold">Module F</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Basic Image & Video Tools</h2>
                </div>
                <span className="px-3 py-1 bg-emerald-500/20 text-emerald-300 rounded-full text-xs font-medium">Asset Management & Editing</span>
              </div>

              <div className="grid md:grid-cols-3 gap-4">
                {[
                  { title: 'Image Collections', desc: 'Organize generated characters, outfits, and scene variants into private tag-based collections.' },
                  { title: 'Basic Image Editing', desc: 'Crop, color grading, exposure adjustment, and retouching controls for final renders.' },
                  { title: 'Short Video Creation', desc: 'Explore image-to-video motion generation subject to technical feasibility and hardware costs.' }
                ].map((tool, idx) => (
                  <div key={idx} className="bg-slate-950 p-5 rounded-xl border border-slate-800">
                    <h3 className="font-semibold text-white text-sm mb-2 text-emerald-300">{tool.title}</h3>
                    <p className="text-xs text-slate-400 leading-relaxed mb-4">{tool.desc}</p>
                    <span className="text-[10px] bg-slate-900 px-2.5 py-1 rounded text-slate-500 border border-slate-800">Module F Spec</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 8. TECH & REALITY CHECK TAB */}
        {activeTab === 'tech' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-amber-400 font-semibold">Technical Evaluation</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Reality Check & Architecture</h2>
                </div>
                <span className="px-3 py-1 bg-amber-500/20 text-amber-300 rounded-full text-xs font-medium">Rule 4 Compliance</span>
              </div>

              <div className="grid md:grid-cols-2 gap-6">
                <div className="bg-slate-950 p-5 rounded-xl border border-slate-800 space-y-3">
                  <h3 className="text-sm font-semibold text-amber-300 flex items-center gap-2">
                    <Cpu className="w-4 h-4" /> Technical Evaluation Factors
                  </h3>
                  <ul className="space-y-2 text-xs text-slate-300">
                    <li className="flex items-start gap-2">
                      <CheckSquare className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                      <span><strong>Virtual Try-On Quality:</strong> Requires advanced ControlNet & Garment-Warping pipelines to preserve lace, straps, and textures accurately.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <CheckSquare className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                      <span><strong>Character Identity Consistency:</strong> Requires LoRA training or InstantID / IP-Adapter integration for stable face generation.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <CheckSquare className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                      <span><strong>Image-to-Video Feasibility:</strong> High VRAM requirements; best evaluated after open-source video models mature.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <CheckSquare className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                      <span><strong>Android Compatibility:</strong> App stream or local quantized runtime evaluation pending hardware benchmarks.</span>
                    </li>
                  </ul>
                </div>

                <div className="bg-slate-950 p-5 rounded-xl border border-slate-800 space-y-3">
                  <h3 className="text-sm font-semibold text-emerald-300 flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4" /> Future Activation Procedure (Rule 8)
                  </h3>
                  <ol className="space-y-2 text-xs text-slate-300 list-decimal list-inside">
                    <li>Review project charter and current priorities.</li>
                    <li>Audit available open-source technologies and local-first runtimes.</li>
                    <li>Select the simplest feasible architecture.</li>
                    <li>Build one limited proof of concept.</li>
                    <li>Test output quality and device compatibility.</li>
                    <li>Decide whether further development is justified.</li>
                  </ol>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 9. GEMINI AI ASSISTANT TAB */}
        {activeTab === 'ai-assistant' && (
          <div className="space-y-6">
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-xs uppercase tracking-widest text-purple-400 font-semibold">Gemini AI Integration</span>
                  <h2 className="text-xl md:text-2xl font-bold text-white">Studio Concept & Prompt Generator</h2>
                </div>
                <span className="px-3 py-1 bg-purple-500/20 text-purple-300 rounded-full text-xs font-medium">Powered by Gemini 2.5 Flash</span>
              </div>

              <form onSubmit={handleGenerateAI} className="space-y-4">
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Target Studio Module</label>
                    <select 
                      value={aiModuleSelection}
                      onChange={(e) => setAiModuleSelection(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-purple-500"
                    >
                      <option>Module A — Fictional Character Studio</option>
                      <option>Module B — Virtual Clothing Try-On</option>
                      <option>Module C — Fashion & Lingerie Studio</option>
                      <option>Module D — Wedding & Formal Wear Studio</option>
                      <option>Module E — Scene & Story Studio</option>
                      <option>Module F — Basic Image & Video Tools</option>
                      <option>General Studio Architecture & Technical Evaluation</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-xs text-slate-400 block mb-1">Quick Action Presets</label>
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setAiPrompt('Design a complete fictional adult character persona with detailed facial features, clothing, and aesthetic prompt tags.')}
                        className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 rounded-lg border border-slate-700 truncate"
                      >
                        Character Persona
                      </button>
                      <button
                        type="button"
                        onClick={() => setAiPrompt('Evaluate the technical challenges of preserving lace and fabric textures during virtual clothing try-on.')}
                        className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-xs text-slate-300 rounded-lg border border-slate-700 truncate"
                      >
                        Try-On Technical Audit
                      </button>
                    </div>
                  </div>
                </div>

                <div>
                  <label className="text-xs text-slate-400 block mb-1">Prompt / Inquiry for Gemini Studio Assistant</label>
                  <textarea 
                    rows={4}
                    value={aiPrompt}
                    onChange={(e) => setAiPrompt(e.target.value)}
                    placeholder="Enter prompt or select a preset..."
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-sm text-white focus:outline-none focus:border-purple-500"
                  ></textarea>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-xs text-slate-500">Requires valid GEMINI_API_KEY secret configured in AI Studio.</span>
                  <button 
                    type="submit" 
                    disabled={aiLoading}
                    className="px-5 py-2.5 bg-purple-600 hover:bg-purple-500 disabled:opacity-50 text-white rounded-lg text-xs font-semibold transition flex items-center gap-2 shadow-lg shadow-purple-900/40"
                  >
                    {aiLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
                    {aiLoading ? 'Generating with Gemini...' : 'Generate AI Concept'}
                  </button>
                </div>
              </form>

              {aiError && (
                <div className="mt-6 p-4 bg-rose-950/40 border border-rose-500/40 rounded-xl text-rose-300 text-xs">
                  <strong>Generation Notice:</strong> {aiError}
                </div>
              )}

              {aiResult && (
                <div className="mt-6 bg-slate-950 border border-slate-800 rounded-xl p-5 space-y-3">
                  <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                    <span className="text-xs font-semibold text-purple-400 flex items-center gap-1.5">
                      <Sparkles className="w-4 h-4" /> Gemini AI Studio Output ({aiModuleSelection})
                    </span>
                    <button 
                      onClick={() => navigator.clipboard.writeText(aiResult)}
                      className="text-xs text-slate-400 hover:text-white bg-slate-900 px-2.5 py-1 rounded border border-slate-800"
                    >
                      Copy Output
                    </button>
                  </div>
                  <div className="text-xs md:text-sm text-slate-300 whitespace-pre-line leading-relaxed font-mono">
                    {aiResult}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-slate-900/60 border-t border-slate-800 py-4 px-4 text-center text-xs text-slate-500">
        <p>Personal AI Creative Studio — Owner: Siavash Ali-Miri — Status: PARKED (Future Project)</p>
      </footer>
    </div>
  );
}
