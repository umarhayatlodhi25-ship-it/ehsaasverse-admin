import { useState, useEffect, useRef } from 'react';
import { getPhotoShayaris, addPhotoShayari, deletePhotoShayari, uploadImage } from '../services/photoShayariService';
import { addToQueue } from '../services/dailyQueueService';
import { getCategories } from '../services/categoryService';
import { Plus, Trash2, Loader2, Image as ImageIcon, Layers, Send, UploadCloud, X } from 'lucide-react';
import toast from 'react-hot-toast';

const PhotoShayari = () => {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeCategory, setActiveCategory] = useState(null);
  
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [uploading, setUploading] = useState(false);

  // Upload Form
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);
  const [categoryId, setCategoryId] = useState('');
  const [sendToQueue, setSendToQueue] = useState(false);
  const fileInputRef = useRef(null);

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
      const data = await getPhotoShayaris(catId);
      setItems(data);
    } catch (err) {
      toast.error('Failed to load photos');
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
      fetchShayaris(catId);
    }
  };

  const handleFilesAdded = (files) => {
    const validFiles = Array.from(files).filter(file => file.type.startsWith('image/'));
    if (validFiles.length === 0) return toast.error('Only image files are allowed');
    
    setSelectedFiles(prev => [...prev, ...validFiles]);
    
    // Generate previews
    const newPreviews = validFiles.map(file => URL.createObjectURL(file));
    setPreviewUrls(prev => [...prev, ...newPreviews]);
  };

  const handleFileChange = (e) => {
    if (e.target.files) {
      handleFilesAdded(e.target.files);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (e.dataTransfer.files) {
      handleFilesAdded(e.dataTransfer.files);
    }
  };

  const removeFile = (index) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
    setPreviewUrls(prev => {
      const newUrls = [...prev];
      URL.revokeObjectURL(newUrls[index]);
      newUrls.splice(index, 1);
      return newUrls;
    });
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!categoryId) return toast.error('Category is required');
    if (selectedFiles.length === 0) return toast.error('Please select images');

    setUploading(true);
    try {
      const categoryObj = categories.find(c => c.id === categoryId);
      
      for (const file of selectedFiles) {
        const url = await uploadImage(file);
        
        if (sendToQueue) {
          await addToQueue({
            type: 'photo',
            imageUrl: url,
            categoryId: categoryObj.id,
            categoryName: categoryObj.name
          });
        } else {
          await addPhotoShayari({
            imageUrl: url,
            categoryId: categoryObj.id,
            categoryName: categoryObj.name
          });
        }
      }
      
      if (sendToQueue) {
        toast.success(`${selectedFiles.length} Photos sent to Daily Queue!`);
      } else {
        toast.success(`${selectedFiles.length} Photos uploaded successfully!`);
      }
      
      setIsUploadOpen(false);
      setSelectedFiles([]);
      previewUrls.forEach(url => URL.revokeObjectURL(url));
      setPreviewUrls([]);
      setCategoryId('');
      setSendToQueue(false);
      
      if (!sendToQueue) fetchShayaris(activeCategory);
    } catch (err) {
      toast.error('Upload failed. Try again.');
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id, url) => {
    if (confirm("Are you sure you want to delete this photo?")) {
      await deletePhotoShayari(id, url);
      toast.success('Deleted');
      fetchShayaris(activeCategory);
    }
  };

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
          {categories.filter(c => c.type !== 'text').map(cat => (
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
            <h2 className="text-2xl font-bold text-white drop-shadow-sm">Photo Shayari</h2>
            <p className="text-sm text-gold/70">
              {activeCategory ? `Showing ${categories.find(c => c.id === activeCategory)?.name} photos` : 'Manage all image-based poetry content'}
            </p>
          </div>
          <button
            onClick={() => { setIsUploadOpen(true); }}
            className="bg-gradient-to-r from-primary to-primary-light text-white px-4 py-2 rounded-xl font-bold flex items-center shadow-[0_0_15px_rgba(139,26,36,0.5)] hover:scale-105 transition-transform"
          >
            <ImageIcon className="w-5 h-5 mr-2" />
            Upload Photos
          </button>
        </div>

        {/* Mobile Categories Dropdown */}
        <div className="md:hidden">
          <select
            value={activeCategory || ''}
            onChange={(e) => handleCategoryClick(e.target.value || null)}
            className="w-full p-3 bg-surface border border-primary-light/20 text-white shadow-sm rounded-xl outline-none"
          >
            <option value="">All Categories</option>
            {categories.filter(c => c.type !== 'text').map(cat => (
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
            <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
              {items.map((item) => (
                <div key={item.id} className="glass-card rounded-2xl shadow-sm border border-primary-light/20 overflow-hidden group relative hover:shadow-[0_0_15px_rgba(229,198,138,0.15)] transition-all">
                  <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity z-10">
                    <button
                      onClick={() => handleDelete(item.id, item.imageUrl)}
                      className="p-2 bg-red-900/80 text-red-100 rounded-lg hover:bg-red-900 shadow-md backdrop-blur-sm"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  <img src={item.imageUrl} alt="Shayari" loading="lazy" className="w-full h-64 object-cover group-hover:scale-105 transition-transform duration-300" />
                  <div className="p-3 absolute bottom-0 w-full bg-gradient-to-t from-background via-background/80 to-transparent pt-8">
                    <span className="text-[10px] uppercase font-black text-background bg-gold-light px-2 py-1 rounded-md shadow-sm">
                      {item.categoryName}
                    </span>
                  </div>
                </div>
              ))}
              {items.length === 0 && <p className="col-span-full text-center py-10 text-gold/50">No photos found</p>}
            </div>
          )}
        </div>
      </div>

      {/* Advanced Bulk Upload Modal */}
      {isUploadOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-surface w-full max-w-2xl rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh] border border-primary-light/20">
            <div className="bg-gradient-to-r from-primary to-primary-dark p-6 text-gold-light flex justify-between items-center shrink-0 border-b border-primary-light/20">
              <h3 className="text-xl font-bold flex items-center"><UploadCloud className="mr-2" /> Drag & Drop Upload</h3>
              <button onClick={() => setIsUploadOpen(false)} className="hover:rotate-90 transition-transform text-gold-light">
                <Plus className="rotate-45 w-8 h-8" />
              </button>
            </div>
            
            <form onSubmit={handleUpload} className="p-8 space-y-6 overflow-y-auto flex-1">
              <div className="space-y-2">
                <label className="text-sm font-bold text-gold/60 uppercase">Target Category *</label>
                <select
                  required
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="w-full bg-background border border-primary-light/20 text-white rounded-2xl p-4 focus:ring-2 focus:ring-gold outline-none"
                >
                  <option value="" disabled>Select Category</option>
                  {categories.filter(c => c.type !== 'text').map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>

              {/* Drag and Drop Zone */}
              <div 
                className="w-full border-2 border-dashed border-gold/30 rounded-3xl p-10 flex flex-col items-center justify-center bg-gold/5 hover:bg-gold/10 transition-colors cursor-pointer"
                onDragOver={(e) => e.preventDefault()}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <UploadCloud className="w-12 h-12 text-gold/50 mb-4" />
                <p className="text-gold-light font-semibold text-center mb-1">Click or drag images here to upload</p>
                <p className="text-gold/50 text-sm text-center">Supports JPG, PNG, WEBP</p>
                <input
                  type="file"
                  multiple
                  accept="image/*"
                  onChange={handleFileChange}
                  ref={fileInputRef}
                  className="hidden"
                />
              </div>

              {/* Image Previews */}
              {previewUrls.length > 0 && (
                <div className="grid grid-cols-3 sm:grid-cols-4 gap-4">
                  {previewUrls.map((url, index) => (
                    <div key={url} className="relative aspect-square rounded-xl overflow-hidden group border border-primary-light/20">
                      <img src={url} alt={`Preview ${index}`} className="w-full h-full object-cover" />
                      <button 
                        type="button" 
                        onClick={(e) => { e.stopPropagation(); removeFile(index); }}
                        className="absolute top-1 right-1 bg-red-900/80 hover:bg-red-900 text-red-100 p-1 rounded-md opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-sm"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}

              {/* Toggle to Send to Queue or Publish Now */}
              <div className="flex items-center gap-3 p-4 bg-background/50 rounded-2xl border border-primary-light/20">
                <input
                  type="checkbox"
                  id="sendToQueuePhoto"
                  checked={sendToQueue}
                  onChange={(e) => setSendToQueue(e.target.checked)}
                  className="w-5 h-5 accent-gold cursor-pointer"
                />
                <label htmlFor="sendToQueuePhoto" className="font-semibold text-gold-light cursor-pointer select-none">
                  Add these {selectedFiles.length} photos to Daily Queue
                </label>
              </div>

            </form>
            <div className="p-6 border-t border-primary-light/20 bg-surface shrink-0">
              <button disabled={uploading || selectedFiles.length === 0} onClick={handleUpload} className="w-full py-4 bg-gradient-to-r from-gold to-gold-dark text-background font-bold rounded-2xl shadow-[0_0_15px_rgba(229,198,138,0.3)] flex justify-center items-center hover:opacity-90 transition-all disabled:opacity-50">
                {uploading ? <Loader2 className="animate-spin w-5 h-5" /> : sendToQueue ? <><Send className="w-5 h-5 mr-2" /> Send to Queue</> : 'Upload Now'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PhotoShayari;
