import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// The spartan theme switches to dark mode via the `dark` class; follow the system preference.
const darkMode = window.matchMedia('(prefers-color-scheme: dark)');
const applyColorScheme = () => document.documentElement.classList.toggle('dark', darkMode.matches);
applyColorScheme();
darkMode.addEventListener('change', applyColorScheme);

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
