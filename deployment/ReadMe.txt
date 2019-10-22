# Create Secret

```bash
./gradlew :deployment:createSecret -PsecretName=foo -PsecretValue=BAR
kubectl get secret foo -o yaml
```