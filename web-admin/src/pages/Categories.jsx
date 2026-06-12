import { useState, useEffect } from 'react';
import { getCategories, addCategory, updateCategory, deleteCategory } from '../services/categoryService';
import { Layers, Plus, Pencil, Trash2, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

  const [formData, setFormData] = useState({ name: '', type: 'both' });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const data = await getCategories();
      setCategories(data);
    } catch (err) {
      toast.error('Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) return;

    try {
      if (editingItem) {
        await updateCategory(editingItem.id, { name: formData.name, type: formData.type });
        toast.success('Category updated');
      } else {
        await addCategory({ name: formData.name, type: formData.type });
        toast.success('Category added');
      }
      setIsModalOpen(false);
      fetchData();
    } catch (e) {
      toast.error('Failed to save category');
    }
  };

  const handleDelete = async (id) => {
    if (confirm("Are you sure you want to delete this category?")) {
      try {
        await deleteCategory(id);
        toast.success('Deleted successfully');
        fetchData();
      } catch (e) {
        toast.error('Failed to delete');
      }
    }
  };

  const openEdit = (cat) => {
    setEditingItem(cat);
    setFormData({ name: cat.name, type: cat.type || 'both' });
    setIsModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center flex-wrap gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white drop-shadow-sm">Categories</h2>
          <p className="text-sm text-gold/70">Manage shayari categories</p>
        </div>
        <button
          onClick={() => { setEditingItem(null); setFormData({ name: '', type: 'both' }); setIsModalOpen(true); }}
          className="bg-gradient-to-r from-primary to-primary-light text-white px-5 py-2.5 rounded-xl font-bold flex items-center shadow-[0_0_15px_rgba(139,26,36,0.5)] hover:scale-105 transition-transform"
        >
          <Plus className="w-5 h-5 mr-2" />
          Add Category
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-10 h-10 animate-spin text-gold" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {categories.map((cat) => (
            <div key={cat.id} className="glass-card p-6 rounded-2xl shadow-sm border border-primary-light/20 flex flex-col relative group hover:shadow-[0_0_15px_rgba(229,198,138,0.15)] transition-all">
              <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity flex gap-2">
                <button onClick={() => openEdit(cat)} className="p-2 bg-primary/20 text-gold-light rounded-lg hover:bg-primary/40 transition-colors">
                  <Pencil className="w-4 h-4" />
                </button>
                <button onClick={() => handleDelete(cat.id)} className="p-2 bg-red-900/40 text-red-400 rounded-lg hover:bg-red-900/60 transition-colors">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
              <div className="flex items-center mb-4">
                <div className="bg-gold/10 p-3 rounded-xl text-gold-light mr-4 border border-gold/20">
                  <Layers className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-bold text-lg text-white">{cat.name}</h3>
                  <p className="text-sm text-gold/60 capitalize">{cat.type} Type</p>
                </div>
              </div>
            </div>
          ))}
          {categories.length === 0 && <p className="col-span-full text-center py-10 text-gold/50">No categories found</p>}
        </div>
      )}

      {isModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-surface w-full max-w-md rounded-3xl shadow-2xl overflow-hidden border border-primary-light/20">
            <div className="bg-gradient-to-r from-primary to-primary-dark p-6 text-gold-light flex justify-between items-center border-b border-primary-light/20">
              <h3 className="text-xl font-bold">{editingItem ? 'Edit' : 'Create'} Category</h3>
              <button onClick={() => setIsModalOpen(false)} className="hover:rotate-90 transition-transform">
                <Plus className="rotate-45 w-8 h-8" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-8 space-y-6">
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/60 uppercase">Category Name</label>
                <input
                  required
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 focus:ring-2 focus:ring-gold outline-none"
                  placeholder="e.g. Ishq"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/60 uppercase">Type</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({...formData, type: e.target.value})}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 focus:ring-2 focus:ring-gold outline-none"
                >
                  <option value="both">Both (Text & Photo)</option>
                  <option value="text">Text Only</option>
                  <option value="photo">Photo Only</option>
                </select>
              </div>
              <button className="w-full py-4 bg-gradient-to-r from-gold to-gold-dark text-background font-bold rounded-2xl shadow-[0_0_15px_rgba(229,198,138,0.3)] hover:opacity-90 transition-all">
                {editingItem ? 'Update Category' : 'Save Category'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Categories;
