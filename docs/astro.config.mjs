// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import mermaid from 'astro-mermaid';

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
			social: [
                { icon: 'discord', label: 'Discord', href: 'https://discord.gg/ZWxxku9xeq' },
                { icon: 'github', label: 'GitHub', href: 'https://github.com/BTE-France/terracore' }
            ],
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
                {
                    label: 'HTTP module',
                    items: [
                        { autogenerate: { directory: 'http' } },
                    ],
                },
                {
                    label: 'Minecraft module',
                    items: [
                        { autogenerate: { directory: 'minecraft' } },
                    ],
                },
                {
                    label: 'World generation module',
                    items: [
                        { autogenerate: { directory: 'worldgen' } },
                    ],
                },
                {
                    label: 'Contributing to Terracore',
                    items: [
                        { autogenerate: { directory: 'contributing' } },
                    ],
                },
			],
		}),
        mermaid(),
	],
});
