import { useState, useEffect } from 'react';
import { getNotifications, sendNotification } from '../services/notificationService';
import { getCategories } from '../services/categoryService';
import { Loader2, Send } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);

  // Form State
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [categoryId, setCategoryId] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [notifs, cats] = await Promise.all([
        getNotifications(),
        getCategories()
      ]);
      setNotifications(notifs);
      setCategories(cats);
    } catch (e) {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    setIsSending(true);
    try {
      await sendNotification(title, body, categoryId || null);
      toast.success('Notification Sent Successfully!');
      setTitle('');
      setBody('');
      setCategoryId('');
      fetchData();
    } catch (e) {
      toast.error('Error sending notification');
    } finally {
      setIsSending(false);
    }
  };

  if (loading) return (
    <div className="flex justify-center py-20">
      <Loader2 className="w-10 h-10 animate-spin text-gold" />
    </div>
  );

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Send Notification Form */}
      <div className="lg:col-span-1 glass-card p-6 h-fit border border-primary-light/20">
        <h2 className="text-xl font-bold mb-6 text-white flex items-center">
          <Send className="w-5 h-5 mr-2 text-gold" />
          Send Notification
        </h2>
        <form onSubmit={handleSend} className="space-y-5">
          <div>
            <label className="block text-sm font-bold text-gold/70 mb-2 uppercase">Title</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full p-3 bg-background border border-primary-light/20 rounded-xl text-white focus:ring-2 focus:ring-gold outline-none shadow-inner"
              placeholder="e.g. Aaj ki 10 nayi shayari ❤️"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-gold/70 mb-2 uppercase">Body</label>
            <textarea
              required
              value={body}
              onChange={(e) => setBody(e.target.value)}
              className="w-full p-3 bg-background border border-primary-light/20 rounded-xl text-white focus:ring-2 focus:ring-gold outline-none shadow-inner"
              rows="3"
              placeholder="EhsaasVerse open karein aur parhein"
            />
          </div>
          <div>
            <label className="block text-sm font-bold text-gold/70 mb-2 uppercase">Target Category (Optional)</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full p-3 bg-background border border-primary-light/20 rounded-xl text-white focus:ring-2 focus:ring-gold outline-none shadow-inner"
            >
              <option value="">All Users (Broadcast)</option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            <p className="text-xs text-gold/50 mt-2">Leave empty to send to all users</p>
          </div>
          <button
            type="submit"
            disabled={isSending}
            className="w-full bg-gradient-to-r from-gold to-gold-dark text-background py-3 rounded-xl font-bold shadow-[0_0_15px_rgba(229,198,138,0.3)] hover:opacity-90 transition-all disabled:opacity-50 flex justify-center items-center mt-4"
          >
            {isSending ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Send Push Notification'}
          </button>
        </form>
      </div>

      {/* History Table */}
      <div className="lg:col-span-2 glass-card overflow-hidden border border-primary-light/20">
        <div className="p-6 border-b border-primary-light/20 bg-surface/30">
          <h2 className="text-xl font-bold text-white">Notification History</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-primary-dark/30 text-gold-light text-sm">
              <tr>
                <th className="p-4 font-semibold uppercase tracking-wider">Title & Body</th>
                <th className="p-4 font-semibold uppercase tracking-wider">Target</th>
                <th className="p-4 font-semibold uppercase tracking-wider">Sent At</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary-light/10 text-white/90">
              {notifications.map(notif => (
                <tr key={notif.id} className="hover:bg-primary-dark/40 transition-colors">
                  <td className="p-4">
                    <p className="font-bold text-white">{notif.title}</p>
                    <p className="text-sm text-gold/70 mt-1">{notif.body}</p>
                  </td>
                  <td className="p-4">
                    {notif.type === 'all' ? (
                      <span className="px-3 py-1 bg-blue-500/20 text-blue-300 border border-blue-500/30 rounded-full text-xs font-bold uppercase tracking-wide">All Users</span>
                    ) : (
                      <span className="px-3 py-1 bg-purple-500/20 text-purple-300 border border-purple-500/30 rounded-full text-xs font-bold uppercase tracking-wide">Category</span>
                    )}
                  </td>
                  <td className="p-4 text-sm text-gold/50">
                    {notif.sentAt?.toDate().toLocaleString() || 'Just now'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {notifications.length === 0 && <p className="p-8 text-center text-gold/50">No notifications sent yet</p>}
        </div>
      </div>
    </div>
  );
}
