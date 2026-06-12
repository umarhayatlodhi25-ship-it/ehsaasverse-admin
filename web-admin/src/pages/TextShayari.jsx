import { useState, useEffect } from 'react';
import { getTextShayaris, addTextShayari, updateTextShayari, deleteTextShayari, bulkAddTextShayari } from '../services/textShayariService';
import { addToQueue } from '../services/dailyQueueService';
import { getCategories } from '../services/categoryService';
import { Plus, Search, Pencil, Trash2, Loader2, Copy, Send, Layers, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';

const TextShayari = () => {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeCategory, setActiveCategory] = useState(null);
  const [visibleCount, setVisibleCount] = useState(20);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isBulkOpen, setIsBulkOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

  // Single Form State
  const [formData, setFormData] = useState({ content: '', categoryId: '' });
  
  // Bulk Form State
  const [bulkData, setBulkData] = useState({ text: '', categoryId: '', sendToQueue: false });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const cats = await getCategories();
      setCategories(cats);
      await fetchShayaris();
    } catch (err) {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const fetchShayaris = async (catId = null) => {
    setLoading(true);
    try {
      const data = await getTextShayaris(catId);
      setItems(data);
    } catch (err) {
      toast.error('Failed to load: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryClick = (catId) => {
    if (activeCategory === catId) {
      setActiveCategory(null);
      fetchShayaris(null);
    } else {
      setActiveCategory(catId);
      const catName = categories.find(c => c.id === catId)?.name;
      fetchShayaris(catName);
    }
  };

  const handleSaveSingle = async (e) => {
    e.preventDefault();
    if (!formData.categoryId) return toast.error('Category is required');
    
    setLoading(true);
    try {
      const categoryObj = categories.find(c => c.id === formData.categoryId);
      const dataToSave = {
        content: formData.content,
        categoryId: categoryObj.id,
        categoryName: categoryObj.name
      };

      if (editingItem) {
        await updateTextShayari(editingItem.id, dataToSave);
        toast.success('Updated successfully');
      } else {
        await addTextShayari(dataToSave);
        toast.success('Added successfully');
      }
      setIsModalOpen(false);
      fetchShayaris(activeCategory);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleBulkSave = async (e) => {
    e.preventDefault();
    if (!bulkData.categoryId) return toast.error('Category is required for bulk add');
    if (!bulkData.text.trim()) return toast.error('Content is empty');

    setLoading(true);
    try {
      const lines = bulkData.text.split('\n').map(line => line.trim()).filter(line => line.length > 5);
      const uniqueLines = [...new Set(lines)];
      
      if (uniqueLines.length === 0) return toast.error('No valid shayari found');

      const categoryObj = categories.find(c => c.id === bulkData.categoryId);
      
      if (bulkData.sendToQueue) {
        for (const line of uniqueLines) {
          await addToQueue({
            type: 'text',
            content: line,
            categoryId: categoryObj.id,
            categoryName: categoryObj.name
          });
        }
        toast.success(`${uniqueLines.length} Shayaris sent to Daily Queue!`);
      } else {
        const shayarisList = uniqueLines.map(line => ({
          content: line,
          categoryId: categoryObj.id,
          categoryName: categoryObj.name
        }));
        await bulkAddTextShayari(shayarisList);
        toast.success(`${shayarisList.length} Shayaris published immediately!`);
      }

      setIsBulkOpen(false);
      setBulkData({ text: '', categoryId: '', sendToQueue: false });
      if (!bulkData.sendToQueue) fetchShayaris(activeCategory);
    } catch (err) {
      toast.error('Failed to process bulk shayaris');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (confirm("Are you sure you want to delete this?")) {
      await deleteTextShayari(id);
      toast.success('Deleted');
      fetchShayaris(activeCategory);
    }
  };

  const openEdit = (item) => {
    setEditingItem(item);
    setFormData({ content: item.content, categoryId: item.categoryId });
    setIsModalOpen(true);
  };

  const handleSmartFormat = (isBulk = false) => {
    if (isBulk) {
      const formatted = bulkData.text.replace(/\n\s*\n/g, '\n').trim();
      setBulkData({ ...bulkData, text: formatted });
      toast.success('Bulk text formatted!');
    } else {
      const formatted = formData.content.replace(/\n\s*\n/g, '\n').trim();
      setFormData({ ...formData, content: formatted });
      toast.success('Text formatted!');
    }
  };

  const handleGenerateAI = async () => {
    if (!formData.categoryId) {
      toast.error('Please select a category first!');
      return;
    }
    const catName = categories.find(c => c.id === formData.categoryId)?.name;
    if (!catName) return;

    try {
      const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
      if (!apiKey) {
        toast.error('API Key is missing in .env file!');
        return;
      }
      
      toast.loading('Generating Shayari with AI...', { id: 'ai-toast' });
      const { GoogleGenerativeAI } = await import('@google/generative-ai');
      const genAI = new GoogleGenerativeAI(apiKey);
      const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });
      
      const prompt = `Write a deep, poetic, and high-quality 2-line Urdu shayari (in Urdu script) for the category: "${catName}".
      Return ONLY the plain Urdu text, nothing else. No english translation. No quotes.`;
      
      const result = await model.generateContent(prompt);
      const text = result.response.text().trim().replace(/['"]/g, '');
      
      setFormData({ ...formData, content: text });
      toast.success('Generated successfully!', { id: 'ai-toast' });
    } catch (err) {
      console.error(err);
      toast.error('AI Generation Failed: ' + err.message, { id: 'ai-toast' });
    }
  };

  const filteredItems = items.filter(item => item.content.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="flex flex-col md:flex-row gap-6 h-[calc(100vh-8rem)]">
      {/* Left Sidebar Categories */}
      <div className="w-full md:w-64 glass-card p-4 overflow-y-auto shrink-0 hidden md:block">
        <h3 className="text-lg font-bold mb-4 flex items-center text-gold-light">
          <Layers className="w-5 h-5 mr-2" />
          Categories
        </h3>
        <ul className="space-y-2">
          <li>
            <button
              onClick={() => handleCategoryClick(null)}
              className={`w-full text-left px-4 py-2 rounded-xl transition-colors ${activeCategory === null ? 'bg-gradient-to-r from-gold to-gold-dark text-background font-bold shadow-md' : 'hover:bg-primary-light/10 text-gold/70 hover:text-gold'}`}
            >
              All Categories
            </button>
          </li>
          {categories.filter(c => c.type !== 'photo').map(cat => (
            <li key={cat.id}>
              <button
                onClick={() => handleCategoryClick(cat.id)}
                className={`w-full text-left px-4 py-2 rounded-xl transition-colors ${activeCategory === cat.id ? 'bg-gradient-to-r from-gold to-gold-dark text-background font-bold shadow-md' : 'hover:bg-primary-light/10 text-gold/70 hover:text-gold'}`}
              >
                {cat.name}
              </button>
            </li>
          ))}
        </ul>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 space-y-6">
        {/* Header & Tools */}
        <div className="flex justify-between items-center flex-wrap gap-4">
          <div>
            <h2 className="text-2xl font-bold text-white drop-shadow-sm">Text Shayari</h2>
            <p className="text-sm text-gold/70">
              {activeCategory ? `Showing ${categories.find(c => c.id === activeCategory)?.name} shayaris` : 'All text-based poetry'}
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => { setIsBulkOpen(true); }}
              className="glass-card border border-gold/30 text-gold-light px-4 py-2 rounded-xl font-bold hover:bg-gold/10 flex items-center transition-all"
            >
              <Copy className="w-5 h-5 mr-2" />
              Bulk Add / Queue
            </button>
            <button
              onClick={() => { setEditingItem(null); setFormData({ content: '', categoryId: activeCategory || '' }); setIsModalOpen(true); }}
              className="bg-gradient-to-r from-primary to-primary-light text-white px-4 py-2 rounded-xl font-bold flex items-center shadow-[0_0_15px_rgba(139,26,36,0.5)] hover:scale-105 transition-transform"
            >
              <Plus className="w-5 h-5 mr-2" />
              Add Single
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="relative w-full">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gold/50 w-5 h-5" />
          <input
            type="text"
            placeholder="Search shayari content..."
            className="w-full pl-10 pr-4 py-3 bg-surface/50 border border-primary-light/20 text-white placeholder-gold/40 shadow-sm rounded-xl focus:ring-2 focus:ring-gold outline-none backdrop-blur-md"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {/* Mobile Categories */}
        <div className="md:hidden">
          <select
            value={activeCategory || ''}
            onChange={(e) => handleCategoryClick(e.target.value || null)}
            className="w-full p-3 bg-surface border border-primary-light/20 text-white shadow-sm rounded-xl outline-none"
          >
            <option value="">All Categories</option>
            {categories.filter(c => c.type !== 'photo').map(cat => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>

        {/* Grid List */}
        <div className="flex-1 overflow-y-auto pb-6">
          {loading ? (
            <div className="flex justify-center py-20">
              <Loader2 className="w-10 h-10 animate-spin text-gold" />
            </div>
          ) : (
            <div className="space-y-6">
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {filteredItems.slice(0, visibleCount).map((item) => (
                  <div key={item.id} className="glass-card p-6 hover:shadow-[0_0_15px_rgba(229,198,138,0.15)] transition-all group relative flex flex-col justify-between border border-primary-light/20">
                    <div>
                      <span className="text-[10px] uppercase font-black text-background bg-gold-light px-2 py-1 rounded-md mb-4 inline-block shadow-sm">
                        {item.categoryName}
                      </span>
                      <p className="urdu-font text-2xl mb-4 text-right leading-relaxed text-white" dir="rtl">{item.content}</p>
                    </div>
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity flex justify-end gap-2 border-t pt-4 mt-4 border-primary-light/10">
                      <button
                        onClick={() => openEdit(item)}
                        className="p-2 bg-primary/20 text-gold-light rounded-lg hover:bg-primary/40 transition-colors"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="p-2 bg-red-900/40 text-red-400 rounded-lg hover:bg-red-900/60 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              {filteredItems.length > visibleCount && (
                <div className="flex justify-center mt-6">
                  <button 
                    onClick={() => setVisibleCount(v => v + 20)}
                    className="px-6 py-2 glass-card text-gold-light font-bold hover:bg-white/5 transition-colors"
                  >
                    Load More
                  </button>
                </div>
              )}
              {filteredItems.length === 0 && <p className="col-span-full text-center py-10 text-gold/50">No records found</p>}
            </div>
          )}
        </div>
      </div>

      {/* Single Add/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-surface w-full max-w-xl rounded-3xl shadow-2xl overflow-hidden border border-primary-light/20">
            <div className="bg-gradient-to-r from-primary to-primary-dark p-6 text-gold-light flex justify-between items-center border-b border-primary-light/20">
              <h3 className="text-xl font-bold">{editingItem ? 'Edit' : 'Create'} Shayari</h3>
              <button onClick={() => setIsModalOpen(false)} className="hover:rotate-90 transition-transform">
                <Plus className="rotate-45 w-8 h-8" />
              </button>
            </div>
            <form onSubmit={handleSaveSingle} className="p-8 space-y-6">
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/60 uppercase">Category *</label>
                <select
                  required
                  value={formData.categoryId}
                  onChange={(e) => setFormData({...formData, categoryId: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 rounded-2xl p-4 text-white focus:ring-2 focus:ring-gold outline-none"
                >
                  <option value="" disabled>Select Category</option>
                  {categories.filter(c => c.type !== 'photo').map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-sm font-bold text-gold/60 uppercase">Urdu Content *</label>
                  <div className="flex gap-4">
                    <button type="button" onClick={handleGenerateAI} className="text-xs text-blue-400 font-bold hover:underline flex items-center gap-1">
                      <Sparkles className="w-3 h-3" /> Generate AI
                    </button>
                    <button type="button" onClick={() => handleSmartFormat(false)} className="text-xs text-gold hover:underline">
                      Clean Format ✨
                    </button>
                  </div>
                </div>
                <textarea
                  required
                  rows="4"
                  dir="rtl"
                  value={formData.content}
                  onChange={(e) => setFormData({...formData, content: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 urdu-font text-2xl focus:ring-2 focus:ring-gold outline-none"
                  placeholder="یہاں کلام لکھیں..."
                />
              </div>
              <button disabled={loading} className="w-full py-4 bg-gradient-to-r from-gold to-gold-dark text-background font-bold rounded-2xl shadow-[0_0_15px_rgba(229,198,138,0.3)] hover:opacity-90 transition-all disabled:opacity-50">
                {loading ? 'Saving...' : (editingItem ? 'Update' : 'Publish')}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Bulk Add Modal */}
      {isBulkOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-surface w-full max-w-3xl rounded-3xl shadow-2xl overflow-hidden border border-gold/20">
            <div className="bg-gradient-to-r from-gold to-gold-dark p-6 text-background flex justify-between items-center border-b border-gold/30">
              <h3 className="text-xl font-bold">Bulk Add / Daily Queue</h3>
              <button onClick={() => setIsBulkOpen(false)} className="hover:rotate-90 transition-transform">
                <Plus className="rotate-45 w-8 h-8" />
              </button>
            </div>
            <form onSubmit={handleBulkSave} className="p-8 space-y-6">
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/70 uppercase">Target Category *</label>
                <select
                  required
                  value={bulkData.categoryId}
                  onChange={(e) => setBulkData({...bulkData, categoryId: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 focus:ring-2 focus:ring-gold outline-none"
                >
                  <option value="" disabled>Select Category</option>
                  {categories.filter(c => c.type !== 'photo').map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/70 uppercase flex justify-between items-center">
                  <span>Paste Multiple Shayaris (1 per line) *</span>
                  <button type="button" onClick={() => handleSmartFormat(true)} className="text-xs text-gold hover:underline">
                    Clean Format ✨
                  </button>
                </label>
                <textarea
                  required
                  rows="8"
                  dir="rtl"
                  value={bulkData.text}
                  onChange={(e) => setBulkData({...bulkData, text: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 urdu-font text-lg focus:ring-2 focus:ring-gold outline-none"
                  placeholder="ہر لائن پر ایک شعر لکھیں..."
                />
              </div>

              {/* Toggle to Send to Queue or Publish Now */}
              <div className="flex items-center gap-3 p-4 bg-background/50 rounded-2xl border border-primary-light/20">
                <input
                  type="checkbox"
                  id="sendToQueue"
                  checked={bulkData.sendToQueue}
                  onChange={(e) => setBulkData({...bulkData, sendToQueue: e.target.checked})}
                  className="w-5 h-5 accent-gold cursor-pointer"
                />
                <label htmlFor="sendToQueue" className="font-semibold text-gold-light cursor-pointer select-none">
                  Add these to Daily Queue (do not publish immediately)
                </label>
              </div>

              <button disabled={loading} className="w-full py-4 bg-gradient-to-r from-primary to-primary-light border border-primary-light/50 text-white font-bold flex justify-center items-center rounded-2xl shadow-[0_0_20px_rgba(139,26,36,0.4)] hover:opacity-90 transition-all disabled:opacity-50">
                {loading ? <Loader2 className="animate-spin w-5 h-5" /> : bulkData.sendToQueue ? <><Send className="w-5 h-5 mr-2" /> Send to Queue</> : 'Publish All Immediately'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TextShayari;
