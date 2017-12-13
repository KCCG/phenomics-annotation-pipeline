docker tag phenomics-pipepline:1.0 482667004524.dkr.ecr.ap-southeast-2.amazonaws.com/phenomics:latest
eval $(aws ecr get-login --no-include-email | sed 's|https://||')
docker push 482667004524.dkr.ecr.ap-southeast-2.amazonaws.com/phenomics:latest
