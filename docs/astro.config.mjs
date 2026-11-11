// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import mermaid from 'astro-mermaid';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
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
			],
		}),
        mermaid(),
	],
});
