import { WebPlugin } from '@capacitor/core';

import type { ZaloPluginPlugin } from './definitions';

export class ZaloPluginWeb extends WebPlugin implements ZaloPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
