# Compile the project
mvn clean package -DskipTests

# Build the Docker image
docker build -t europe-west9-docker.pkg.dev/jr-website-cf53c/wheelbase-repo/app:latest .

# Push image to GCloud
gcloud auth configure-docker europe-west9-docker.pkg.dev
docker push europe-west9-docker.pkg.dev/jr-website-cf53c/wheelbase-repo/app:latest

# Deploy to Cloud Run
gcloud run deploy wheelbase-app \
  --image europe-west9-docker.pkg.dev/jr-website-cf53c/wheelbase-repo/app:latest \
  --platform managed \
  --region europe-west9 \
  --allow-unauthenticated \
  --add-cloudsql-instances jr-website-cf53c:europe-west9:wheelbase-database \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod \
  --set-secrets DB_USER=db-user:latest,DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest \
  --timeout=600s