/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#140304',
        surface: '#2A080C',
        primary: {
          light: '#b73847',
          DEFAULT: '#8B1A24',
          dark: '#580e15',
        },
        gold: {
          light: '#FCE7B2',
          DEFAULT: '#E5C68A',
          dark: '#B08E4A',
        }
      },
      fontFamily: {
        urdu: ['Noto Nastaliq Urdu', 'Jameel Noori Nastaleeq', 'serif'],
        sans: ['Inter', 'Poppins', 'sans-serif'],
      },
      keyframes: {
        glare: {
          '0%': { transform: 'translateX(-100%)' },
          '100%': { transform: 'translateX(100%)' },
        }
      },
      animation: {
        glare: 'glare 2s ease-in-out infinite',
      }
    },
  },
  plugins: [],
}
