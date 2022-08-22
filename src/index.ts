import { registerPlugin } from '@capacitor/core';

import type { ZaloPluginPlugin } from './definitions';

const ZaloPlugin = registerPlugin<ZaloPluginPlugin>('ZaloPlugin', {
  web: () => import('./web').then(m => new m.ZaloPluginWeb()),
});

export * from './definitions';
export { ZaloPlugin };
