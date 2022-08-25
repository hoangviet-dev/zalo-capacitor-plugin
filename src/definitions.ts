import type { PluginListenerHandle } from "@capacitor/core";

export interface Errorable {
  readonly error?: {
    code: string;
    message: string;
  };
}

export interface LoginResponse extends Errorable {
  readonly success: boolean;
  readonly oauthCode?: string;
}

export interface UserProfile extends Errorable {
  readonly success: boolean;
  readonly id: string;
  readonly name: string;
  readonly gender?: string;
  readonly birthday?: string;
  readonly picture?: {
    readonly data?: {
      readonly url?: string;
    }
  };
}

export interface HashKeyAndroid extends Errorable {
  readonly success: boolean;
  readonly hashKey: string;
}

export interface ZaloPluginPlugin {
  getProfile(): Promise<UserProfile>;
  login(): Promise<LoginResponse>;
  logout(): Promise<void>;
  getHashKeyAndroid(): Promise<HashKeyAndroid>;
  addListener(
    eventName: 'onEvent',
    listenerFunc: (result: any) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
