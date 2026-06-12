import { useState, useEffect } from 'react';
import {
  FileText,
  Image as ImageIcon,
  Layers,
  MessageSquare,
  TrendingUp,
  ArrowUpRight
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';
import { collection, getCountFromServer, query, orderBy, limit, getDocs } from 'firebase/firestore';
import { db } from '../firebase/config';
import { Link } from 'react-router-dom';

const Dashboard = () => {
  const [stats, setStats] = useState({
    textCount: 0,
    photoCount: 0,
    categoryCount: 0,
    queueCount: 0
  });
  const [recentUploads, setRecentUploads] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const textSnap = await getCountFromServer(collection(db, "text_shayari"));
        const photoSnap = await getCountFromServer(collection(db, "photo_shayari"));
        const catSnap = await getCountFromServer(collection(db, "categories"));
        const queueSnap = await getCountFromServer(collection(db, "daily_queue"));

        setStats({
          textCount: textSnap.data().count,
          photoCount: photoSnap.data().count,
          categoryCount: catSnap.data().count,
          queueCount: queueSnap.data().count
        });

        const q = query(collection(db, "text_shayari"), orderBy("createdAt", "desc"), limit(5));
        const recentSnap = await getDocs(q);
        setRecentUploads(recentSnap.docs.map(d => ({ id: d.id, ...d.data() })));
      } catch (e) {
        console.error("Error fetching stats:", e);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const handleDownloadReport = () => {
    const headers = ['Type', 'Total Count'];
    const data = [
      ['Text Shayari', stats.textCount],
      ['Photo Shayari', stats.photoCount],
      ['Categories', stats.categoryCount],
      ['Pending Queue', stats.queueCount]
    ];
    
    let csvContent = "data:text/csv;charset=utf-8," 
      + headers.join(",") + "\n"
      + data.map(e => e.join(",")).join("\n");
      
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `EhsaasVerse_Report_${new Date().toLocaleDateString()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const chartData = [
    { name: 'Mon', text: 12, photos: 4 },
    { name: 'Tue', text: 19, photos: 7 },
    { name: 'Wed', text: 15, photos: 5 },
    { name: 'Thu', text: 22, photos: 8 },
    { name: 'Fri', text: 30, photos: 12 },
    { name: 'Sat', text: 25, photos: 10 },
    { name: 'Sun', text: 18, photos: 6 },
  ];

  if (loading) return <div className="p-10 text-center text-gold">Loading Dashboard...</div>;

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-bold text-white drop-shadow-sm">Admin Dashboard</h2>
          <p className="text-gold/70">Welcome to EhsaasVerse Management Hub.</p>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={handleDownloadReport}
            className="flex items-center px-4 py-2 glass-card text-gold-light hover:bg-white/10 transition-all font-medium"
          >
            Download Report
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          icon={FileText}
          label="Total Text Shayari"
          value={stats.textCount}
          color="bg-primary-light"
        />
        <StatCard
          icon={ImageIcon}
          label="Total Photo Shayari"
          value={stats.photoCount}
          color="bg-purple-600"
        />
        <StatCard
          icon={Layers}
          label="Categories"
          value={stats.categoryCount}
          color="bg-gold-dark"
        />
        <StatCard
          icon={MessageSquare}
          label="Daily Queue Pending"
          value={stats.queueCount}
          color="bg-amber-600"
        />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="glass-card p-6">
          <h3 className="text-lg font-bold mb-6 flex items-center text-white">
            <TrendingUp className="w-5 h-5 mr-2 text-gold" />
            Content Growth
          </h3>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorText" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#E5C68A" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#E5C68A" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#8B1A24" opacity={0.2} />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#FCE7B2', fontSize: 12}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#FCE7B2', fontSize: 12}} />
                <Tooltip contentStyle={{ backgroundColor: '#2A080C', borderColor: '#8B1A24', color: '#fff' }} />
                <Area type="monotone" dataKey="text" stroke="#E5C68A" fillOpacity={1} fill="url(#colorText)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass-card p-6">
          <h3 className="text-lg font-bold mb-6 flex items-center text-white">
            <ImageIcon className="w-5 h-5 mr-2 text-purple-400" />
            Photo Submissions
          </h3>
          <div className="h-[300px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#8B1A24" opacity={0.2} />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#FCE7B2', fontSize: 12}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#FCE7B2', fontSize: 12}} />
                <Tooltip contentStyle={{ backgroundColor: '#2A080C', borderColor: '#8B1A24', color: '#fff' }} />
                <Bar dataKey="photos" fill="#a855f7" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Activity Section */}
      <div className="glass-card overflow-hidden">
        <div className="p-6 border-b border-primary-light/20 flex justify-between items-center">
          <h3 className="text-lg font-bold text-white">Recent Text Uploads</h3>
          <Link to="/text-shayari" className="text-gold hover:text-gold-light font-medium flex items-center text-sm hover:underline">
            View All <ArrowUpRight className="w-4 h-4 ml-1" />
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-primary-dark/30 text-gold-light text-xs uppercase font-bold">
              <tr>
                <th className="px-6 py-4">Content</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary-light/10">
              {recentUploads.map(upload => (
                <ActivityRow
                  key={upload.id}
                  title={upload.content}
                  category={upload.categoryName || 'Unknown'}
                  status={upload.status}
                />
              ))}
              {recentUploads.length === 0 && (
                <tr><td colSpan="3" className="px-6 py-4 text-center text-gold/50">No uploads yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ icon: Icon, label, value, color }) => (
  <div className="glass-card p-6 hover:shadow-[0_0_20px_rgba(229,198,138,0.1)] transition-all">
    <div className="flex items-center justify-between mb-4">
      <div className={`${color} p-3 rounded-xl text-white shadow-lg`}>
        <Icon className="w-6 h-6" />
      </div>
    </div>
    <p className="text-gold/70 text-sm">{label}</p>
    <h4 className="text-2xl font-bold text-white mt-1">{value?.toLocaleString()}</h4>
  </div>
);

const ActivityRow = ({ title, category, status }) => (
  <tr className="hover:bg-primary-dark/40 transition-colors text-white/90">
    <td className="px-6 py-4 font-medium urdu-font text-xl line-clamp-1 truncate max-w-md" dir="rtl">{title}</td>
    <td className="px-6 py-4">
      <span className="px-3 py-1 bg-gold/10 text-gold rounded-full text-xs font-bold uppercase border border-gold/20">{category}</span>
    </td>
    <td className="px-6 py-4">
      <div className="flex items-center">
        <div className={`w-2 h-2 rounded-full mr-2 ${status === 'published' ? 'bg-emerald-400' : 'bg-amber-400 shadow-[0_0_8px_rgba(251,191,36,0.6)]'}`}></div>
        <span className="text-sm text-white/80 capitalize">{status}</span>
      </div>
    </td>
  </tr>
);

export default Dashboard;
