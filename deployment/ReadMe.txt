# Create Secret

```bash
./gradlew :deployment:createSecret -PsecretName=foo -PsecretValue=BAR
kubectl get secret foo -o yaml
```

# Create Application Config

```bash
./gradlew :deployment:createSecret -PsecretName=application-config -PsecretValue=$PWD/app/src/main/resources/application.properties
kubectl get secret application-config -o yaml
```