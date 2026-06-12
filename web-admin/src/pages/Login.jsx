import { useState, useEffect } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase/config';
import { Feather, Lock, Mail, AlertCircle, ArrowRight, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

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
    <div className="relative min-h-screen w-full flex items-center justify-center overflow-hidden bg-background font-sans">
      
      {/* Premium Animated Background */}
      <div className="absolute inset-0 w-full h-full">
        {/* Core background texture */}
        <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/stardust.png')] opacity-30 mix-blend-overlay"></div>
        
        {/* Glowing Orbs */}
        <div className="absolute top-[-20%] left-[-10%] w-[70vw] h-[70vw] max-w-[800px] max-h-[800px] bg-primary rounded-full mix-blend-screen filter blur-[150px] opacity-40 animate-pulse duration-[8000ms]"></div>
        <div className="absolute bottom-[-20%] right-[-10%] w-[60vw] h-[60vw] max-w-[700px] max-h-[700px] bg-gold rounded-full mix-blend-screen filter blur-[120px] opacity-20 animate-pulse duration-[6000ms] delay-1000"></div>
        <div className="absolute top-[40%] left-[60%] w-[40vw] h-[40vw] max-w-[500px] max-h-[500px] bg-[#4A0E13] rounded-full mix-blend-screen filter blur-[100px] opacity-50 animate-pulse duration-[10000ms] delay-2000"></div>
      </div>

      <div className={`relative w-full max-w-[420px] z-10 px-6 transition-all duration-1000 ${mounted ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-12'}`}>
        
        {/* Decorative Top Accent */}
        <div className="flex justify-center mb-[-20px] relative z-20">
          <div className="bg-gradient-to-br from-gold-light to-gold p-[2px] rounded-2xl shadow-[0_0_30px_rgba(229,198,138,0.5)]">
            <div className="bg-background p-4 rounded-2xl flex items-center justify-center">
              <Feather className="w-8 h-8 text-gold drop-shadow-lg animate-bounce duration-[3000ms]" />
            </div>
          </div>
        </div>

        {/* Main Glass Card */}
        <div className="bg-black/40 backdrop-blur-3xl border border-white/10 rounded-t-[40px] rounded-b-[24px] shadow-2xl overflow-hidden pt-12 pb-8 px-8 relative">
          
          {/* Subtle top glare effect */}
          <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/20 to-transparent"></div>

          <div className="text-center mb-8">
            <h1 className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white via-gold-light to-white tracking-tight drop-shadow-sm">EhsaasVerse</h1>
            <div className="flex items-center justify-center gap-2 mt-2">
              <Sparkles className="w-3 h-3 text-gold/80" />
              <p className="text-gold/80 text-xs uppercase tracking-[0.25em] font-semibold">Admin Gateway</p>
              <Sparkles className="w-3 h-3 text-gold/80" />
            </div>
          </div>

          <form onSubmit={handleLogin} className="space-y-6">
            
            {error && (
              <div className="bg-red-500/10 border border-red-500/30 p-3.5 rounded-2xl flex items-start animate-shake shadow-inner">
                <AlertCircle className="w-5 h-5 text-red-400 mr-3 mt-0.5 shrink-0" />
                <p className="text-sm text-red-200/90 font-medium">{error}</p>
              </div>
            )}

            <div className="space-y-5">
              {/* Email Input */}
              <div className="relative group">
                <div className="absolute -inset-0.5 bg-gradient-to-r from-gold/50 to-primary/50 rounded-2xl blur opacity-0 group-focus-within:opacity-100 transition duration-500"></div>
                <div className="relative flex items-center bg-black/50 border border-white/10 rounded-2xl overflow-hidden transition-all duration-300 group-focus-within:bg-black/70 group-focus-within:border-gold/50">
                  <div className="pl-4 pr-3 py-4 flex items-center justify-center bg-transparent">
                    <Mail className="w-5 h-5 text-white/40 group-focus-within:text-gold transition-colors duration-300" />
                  </div>
                  <div className="flex-1 relative">
                    <input
                      type="email"
                      id="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      className="peer w-full bg-transparent text-white placeholder-transparent pt-5 pb-2 pr-4 outline-none text-sm"
                      placeholder="Email"
                    />
                    <label 
                      htmlFor="email"
                      className="absolute left-0 top-1/2 -translate-y-1/2 text-white/40 text-sm transition-all duration-300 peer-focus:-top-1 peer-focus:text-[10px] peer-focus:text-gold peer-focus:font-bold peer-focus:uppercase tracking-wider peer-valid:-top-1 peer-valid:text-[10px] peer-valid:text-gold/70 peer-valid:font-bold peer-valid:uppercase"
                    >
                      Admin Email
                    </label>
                  </div>
                </div>
              </div>

              {/* Password Input */}
              <div className="relative group">
                <div className="absolute -inset-0.5 bg-gradient-to-r from-gold/50 to-primary/50 rounded-2xl blur opacity-0 group-focus-within:opacity-100 transition duration-500"></div>
                <div className="relative flex items-center bg-black/50 border border-white/10 rounded-2xl overflow-hidden transition-all duration-300 group-focus-within:bg-black/70 group-focus-within:border-gold/50">
                  <div className="pl-4 pr-3 py-4 flex items-center justify-center bg-transparent">
                    <Lock className="w-5 h-5 text-white/40 group-focus-within:text-gold transition-colors duration-300" />
                  </div>
                  <div className="flex-1 relative">
                    <input
                      type="password"
                      id="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="peer w-full bg-transparent text-white placeholder-transparent pt-5 pb-2 pr-4 outline-none text-sm"
                      placeholder="Password"
                    />
                    <label 
                      htmlFor="password"
                      className="absolute left-0 top-1/2 -translate-y-1/2 text-white/40 text-sm transition-all duration-300 peer-focus:-top-1 peer-focus:text-[10px] peer-focus:text-gold peer-focus:font-bold peer-focus:uppercase tracking-wider peer-valid:-top-1 peer-valid:text-[10px] peer-valid:text-gold/70 peer-valid:font-bold peer-valid:uppercase"
                    >
                      Password
                    </label>
                  </div>
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`
                relative w-full py-4 mt-2 rounded-2xl bg-gradient-to-r from-gold to-gold-light text-slate-900 font-bold text-base
                shadow-[0_10px_30px_-10px_rgba(229,198,138,0.5)] overflow-hidden group
                ${loading ? 'opacity-80 cursor-not-allowed' : 'hover:shadow-[0_10px_40px_-5px_rgba(229,198,138,0.7)] hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.98]'}
                transition-all duration-300 flex items-center justify-center
              `}
            >
              {/* Button Glare Effect */}
              <div className="absolute inset-0 w-full h-full bg-gradient-to-r from-transparent via-white/40 to-transparent -translate-x-full group-hover:animate-glare"></div>
              
              <span className="relative flex items-center z-10">
                {loading ? (
                  <span className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-slate-900" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Authenticating...
                  </span>
                ) : (
                  <>
                    Sign In Securely
                    <ArrowRight className="w-5 h-5 ml-2 group-hover:translate-x-1.5 transition-transform duration-300" />
                  </>
                )}
              </span>
            </button>
          </form>
        </div>
        
        {/* Footer Text */}
        <div className={`text-center mt-6 transition-all duration-1000 delay-500 ${mounted ? 'opacity-100' : 'opacity-0'}`}>
          <p className="text-white/20 text-[11px] uppercase tracking-[0.3em] font-semibold">
            © {new Date().getFullYear()} EhsaasVerse
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
