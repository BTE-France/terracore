// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
    site: process.env.SITE_URL ?? 'https://terracore.btefrance.fr',
    base: process.env.SITE_BASE ?? '/',
	integrations: [
		starlight({
            components: {
                // Override the default `ThemeSelect` component to inject the version selector dropdown before it
                ThemeSelect: './src/components/ThemeSelect.astro',
            },
			title: 'Terracore documentation',
            logo: {
                src: './src/assets/terracore.svg',
            },
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/BTE-France/terracore' }],
			sidebar: [
				{
					label: 'Getting started',
					items: [
                        { autogenerate: { directory: 'start' } },
					],
				},
                {
                    label: 'Geo module',
                    items: [
                        { autogenerate: { directory: 'geo' } },
                    ],
                },
			],
		}),
	],
});
