import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/app/**/*.{ts,tsx}", "./src/frontend/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ivory: "#f7f4ed",
        limestone: "#e8e1d5",
        espresso: "#29251f",
        olive: "#656b55",
        sand: "#d8c5ad",
        coastal: "#6f888e",
      },
      fontFamily: {
        sans: ["Manrope", "Helvetica Neue", "sans-serif"],
        serif: ["Cormorant Garamond", "Georgia", "serif"],
      },
    },
  },
  plugins: [],
};

export default config;
