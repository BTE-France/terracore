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
					label: 'Guides',
					items: [
						// Each item here is one entry in the navigation menu.
						{ label: 'Example Guide', slug: 'guides/example' },
					],
				},
			],
		}),
        mermaid(),
	],
});
