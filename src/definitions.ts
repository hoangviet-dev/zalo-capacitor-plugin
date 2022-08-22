export interface ZaloPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
