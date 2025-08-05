import { TurboModule, TurboModuleRegistry } from 'react-native';

export interface ShareData {
  mimeType: string;
  data: string;
  fileName: string;
}

export interface Spec extends TurboModule {
  getSharedData(): Promise<ShareData[] | null>;
  shareMedia(files: ShareData[]): Promise<boolean>;
}

export default TurboModuleRegistry.get<Spec>(
  'ReactNativeShareMedia'
) as Spec | null;
