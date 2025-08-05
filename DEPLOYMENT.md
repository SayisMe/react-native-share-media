# 배포 가이드

## 1. GitHub 레포지토리 생성

1. GitHub에서 새 레포지토리를 생성합니다: `react-native-share-media`
2. 레포지토리를 public으로 설정합니다.

## 2. 로컬에서 Git 초기화

```bash
cd ReactNativeShareMedia
git init
git add .
git commit -m "Initial commit: React Native Share Media package"
```

## 3. GitHub 레포지토리 연결

```bash
git remote add origin https://github.com/SayisMe/react-native-share-media.git
git branch -M main
git push -u origin main
```

## 4. npm 계정 설정

```bash
# npm 로그인 (계정이 없다면 먼저 가입)
npm login

# 패키지 이름 확인 (중복되지 않는지)
npm search react-native-share-media
```

## 5. 패키지 배포

```bash
# 패키지 빌드
npm run build

# npm에 배포
npm publish
```

## 6. 버전 업데이트 (필요시)

```bash
# 패키지 버전 업데이트
npm version patch  # 0.0.1 -> 0.0.2
npm version minor  # 0.0.1 -> 0.1.0
npm version major  # 0.0.1 -> 1.0.0

# 변경사항 커밋 및 푸시
git add .
git commit -m "Bump version to X.X.X"
git push

# npm에 배포
npm publish
```

## 주의사항

- npm 패키지 이름이 고유한지 확인하세요
- 배포 전에 `npm pack`으로 패키지 내용을 확인하세요
- README.md가 완성되었는지 확인하세요
- 라이선스가 올바르게 설정되었는지 확인하세요
