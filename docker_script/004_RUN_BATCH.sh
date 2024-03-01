docker run -itd --name docker_batch_app \
    --network docker_batch_network \
    -e SPRING_PROFILES_ACTIVE=docker \
    -v /app/batch/infiles:/infiles \
    docker_batch_app_image