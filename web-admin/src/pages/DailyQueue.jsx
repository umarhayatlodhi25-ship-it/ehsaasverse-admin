import { useState, useEffect } from 'react';
import { getQueueItems, deleteFromQueue, publishDailyShayaris } from '../services/dailyQueueService';
import { Loader2, Trash2, Send } from 'lucide-react';
import toast from 'react-hot-toast';

export default function DailyQueue() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchQueue();
  }, []);

  const fetchQueue = async () => {
    try {
      const data = await getQueueItems();
      setItems(data);
    } catch (e) {
      toast.error('Failed to fetch queue');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (confirm('Are you sure you want to delete this from queue?')) {
      await deleteFromQueue(id);
      toast.success('Removed from queue');
      fetchQueue();
    }
  };

  const handlePublishDaily = async () => {
    try {
      const count = await publishDailyShayaris(10);
      toast.success(`Successfully published ${count} shayaris!`);
      fetchQueue();
    } catch (e) {
      toast.error('Failed to publish');
    }
  };

  if (loading) return (
    <div className="flex justify-center py-20">
      <Loader2 className="w-10 h-10 animate-spin text-gold" />
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white drop-shadow-sm">Daily Queue</h1>
          <p className="text-sm text-gold/70">Manage automated daily publishes</p>
        </div>
        <button
          onClick={handlePublishDaily}
          className="bg-gradient-to-r from-gold to-gold-dark text-background px-6 py-2.5 rounded-xl font-bold hover:opacity-90 shadow-[0_0_15px_rgba(229,198,138,0.3)] transition-all flex items-center"
        >
          <Send className="w-4 h-4 mr-2" />
          Publish 10 Now
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-primary-dark/30 border-b border-primary-light/20 text-gold-light text-sm">
            <tr>
              <th className="p-4 font-semibold uppercase tracking-wider">Type</th>
              <th className="p-4 font-semibold uppercase tracking-wider">Category</th>
              <th className="p-4 font-semibold uppercase tracking-wider">Content / Image</th>
              <th className="p-4 font-semibold uppercase tracking-wider">Status</th>
              <th className="p-4 font-semibold uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-primary-light/10 text-white/90">
            {items.map(item => (
              <tr key={item.id} className="hover:bg-primary-dark/40 transition-colors">
                <td className="p-4 capitalize">{item.type}</td>
                <td className="p-4">
                  <span className="bg-gold/10 text-gold-light px-2 py-1 rounded-md text-xs border border-gold/20 font-bold tracking-wide">
                    {item.categoryName}
                  </span>
                </td>
                <td className="p-4">
                  {item.type === 'photo' ? (
                    <img src={item.imageUrl} alt="Shayari" className="w-16 h-16 object-cover rounded-lg border border-primary-light/20 shadow-sm" />
                  ) : (
                    <p className="urdu-font text-xl line-clamp-2 max-w-md" dir="rtl">{item.content}</p>
                  )}
                </td>
                <td className="p-4">
                  <span className={`px-2 py-1 rounded-full text-xs font-bold shadow-sm ${item.status === 'published' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-amber-500/20 text-amber-400 border border-amber-500/30 shadow-[0_0_8px_rgba(251,191,36,0.3)]'}`}>
                    {item.status}
                  </span>
                </td>
                <td className="p-4">
                  {item.status !== 'published' && (
                    <button onClick={() => handleDelete(item.id)} className="p-2 bg-red-900/40 text-red-400 rounded-lg hover:bg-red-900/60 transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {items.length === 0 && <p className="p-8 text-center text-gold/50">Queue is empty</p>}
      </div>
    </div>
  );
}
