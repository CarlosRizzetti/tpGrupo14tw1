/** @type {import('tailwindcss').Config} */
const defaultTheme = require('tailwindcss/defaultTheme');

module.exports = {
  content: [
    "./src/main/java/com/tallerwebi/**/*.java",
    "./resources/**/*.blade.php",
    "./resources/**/*.js",
    "./resources/**/*.vue",
    "./app/Models/Category.php",
    "./src/main/webapp/WEB-INF/views/jsp/**/*.jsp",
    "./src/main/webapp/WEB-INF/views/**/*.html"
  ],
  safelist: [
    'tema-cocina',
    'tema-mccafe',
    'tema-isla',
    'tema-servicio',
    'tema-login',
    'bg-puesto-soft',
    'animate-success'
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Figtree', ...defaultTheme.fontFamily.sans],
      },
      colors: {
        puesto: {
          app: 'var(--bg-app)',
          header: 'var(--bg-header)',
          'header-text': 'var(--text-header)',
          btn: 'var(--btn-bg)',
          'btn-text': 'var(--btn-text)',
          'btn-border': 'var(--btn-border)',
          'btn-hover-bg': 'var(--btn-hover)',
          accent: 'var(--icon-color)',
        }
      }
    }
  }
}