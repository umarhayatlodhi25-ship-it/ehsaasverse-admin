import { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase/config';
import { Feather, Lock, Mail, AlertCircle, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await signInWithEmailAndPassword(auth, email, password);
      toast.success('Welcome back, Admin!');
    } catch (err) {
      setError('Invalid admin credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative h-screen w-full flex items-center justify-center overflow-hidden bg-slate-900">
      {/* Animated Background Gradients */}
      <div className="absolute top-[-20%] left-[-10%] w-[70%] h-[70%] bg-primary rounded-full mix-blend-screen filter blur-[120px] opacity-40 animate-pulse"></div>
      <div className="absolute bottom-[-20%] right-[-10%] w-[60%] h-[60%] bg-gold rounded-full mix-blend-screen filter blur-[120px] opacity-20 animate-pulse delay-1000"></div>

      <div className="relative w-full max-w-md z-10 p-4">
        <div className="bg-white/10 backdrop-blur-2xl border border-white/20 rounded-3xl shadow-2xl overflow-hidden">
          <div className="p-10 text-center">
            <div className="inline-flex p-4 rounded-2xl bg-white/5 border border-white/10 mb-6 backdrop-blur-md shadow-inner">
              <Feather className="w-10 h-10 text-gold" />
            </div>
            <h1 className="text-3xl font-bold text-white tracking-tight">EhsaasVerse</h1>
            <p className="text-gold/80 text-sm mt-2 uppercase tracking-[0.2em] font-medium">Secure Admin Gateway</p>
          </div>

          <div className="p-8 pt-0 space-y-6">
            {error && (
              <div className="bg-red-500/10 border border-red-500/50 p-4 rounded-xl flex items-start animate-shake">
                <AlertCircle className="w-5 h-5 text-red-400 mr-3 mt-0.5 shrink-0" />
                <p className="text-sm text-red-200">{error}</p>
              </div>
            )}

            <form onSubmit={handleLogin} className="space-y-5">
              <div>
                <label className="block text-[10px] font-bold text-white/50 uppercase tracking-widest mb-2 ml-1">Admin Email</label>
                <div className="relative group">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-white/40 group-focus-within:text-gold transition-colors" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full bg-white/5 border border-white/10 text-white placeholder-white/30 rounded-2xl py-4 pl-12 pr-4 focus:ring-2 focus:ring-gold focus:border-transparent transition-all outline-none"
                    placeholder="admin@ehsaasverse.com"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[10px] font-bold text-white/50 uppercase tracking-widest mb-2 ml-1">Password</label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-white/40 group-focus-within:text-gold transition-colors" />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="w-full bg-white/5 border border-white/10 text-white placeholder-white/30 rounded-2xl py-4 pl-12 pr-4 focus:ring-2 focus:ring-gold focus:border-transparent transition-all outline-none"
                    placeholder="••••••••"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className={`
                  w-full py-4 mt-4 rounded-2xl bg-gold text-slate-900 font-bold shadow-[0_0_20px_rgba(229,198,138,0.3)]
                  hover:bg-gold-light hover:shadow-[0_0_30px_rgba(229,198,138,0.5)] transition-all flex items-center justify-center group
                  ${loading ? 'opacity-70 cursor-not-allowed' : 'active:scale-[0.98]'}
                `}
              >
                {loading ? 'Authenticating...' : (
                  <>
                    Sign In Securely
                    <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1 transition-transform" />
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
        
        <p className="text-center text-white/30 text-xs mt-8 tracking-wider">
          © {new Date().getFullYear()} EhsaasVerse. Restricted Access.
        </p>
      </div>
    </div>
  );
};

export default Login;
