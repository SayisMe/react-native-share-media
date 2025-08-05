<<<<<<< HEAD
# React Native Share Media

React Native TurboModule for sharing media files between apps.

## Installation

```bash
npm install react-native-share-media
# or
yarn add react-native-share-media
```

## Usage

```typescript
import ReactNativeShareMedia from 'react-native-share-media';

// Get shared data from other apps
const sharedData = await ReactNativeShareMedia.getSharedData();

// Share media files
const files = [
  {
    mimeType: 'image/jpeg',
    data: 'base64-encoded-data',
    fileName: 'image.jpg',
  },
];
const success = await ReactNativeShareMedia.shareMedia(files);
```

## API

### `getSharedData(): Promise<ShareData[] | null>`

Retrieves shared data from other apps. Returns an array of shared files or null if no data is available.

### `shareMedia(files: ShareData[]): Promise<boolean>`

Shares media files to other apps. Returns true if sharing was successful.

### Types

```typescript
interface ShareData {
  mimeType: string;
  data: string;
  fileName: string;
}
```

## Requirements

- React Native 0.60+
- Android API level 21+

## License

MIT
=======
# react-native-share-media
>>>>>>> b84a9416027b5442d6c84b755d9459d022e28105
