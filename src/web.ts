import { WebPlugin } from '@capacitor/core';

import type { HashKeyAndroid, ZaloPluginPlugin, LoginResponse, UserProfile } from './definitions';

export class ZaloPluginWeb extends WebPlugin implements ZaloPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async login(): Promise<LoginResponse> {
    console.log('[login] Web version is not supported');
    return Promise.resolve({
      success: false,
    });
  }

  async getProfile(): Promise<UserProfile> {
    console.log('[getUserProfile] Web version is not supported');
    return { success: false, id: '', name: '' };
  }

  async logout(): Promise<void>  {
    console.log('[logout] Web version is not supported');
  }

  async getHashKeyAndroid(): Promise<HashKeyAndroid>  {
    console.log('[getHashKeyAndroid] Web version is not supported');
    return {
      hashKey: '',
      success: false
    }
  }
}
