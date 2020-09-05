# learning-chaos-springboot

[![azure](https://img.shields.io/badge/azure-yellow.svg)](https://portal.azure.com)
[![azure cli](https://img.shields.io/badge/azurecli-blue.svg)](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest)
[![kubectl](https://img.shields.io/badge/kubectl-red.svg)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

> Instructions to deploy Java Spring Boot Microservices for Chaos Engineering as an Azure Kubernetes Service.
>
> Developed by Chaos Guild Team.

## Prerequisites

You will need the following things properly installed on your computer.

- [Docker](https://www.docker.com/)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest)
- [Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

## Deploy on Azure AKS

- Login to Azure CLI.

```bash
az login
```

- Export your environment variables.

```bash
export RESOURCE_GROUP=app-chaosspringboot-rg
export REGISTRY=chaosspringbootacr
export REGION=eastus
export PLAN=app-chaosspringboot-plan
export APP_NAME=app-chaosspringboot

export VNET_NAME=aks-vnet
export SUBNET_NAME=aks-subnet
export AKS_CLUSTER_NAME=aks-chaos
```

### Build Docker Images

- Create the Azure Resource Group if you don't have an existing one.

```bash
  az group create -n $RESOURCE_GROUP -l $REGION
```

- Create an Azure Container Registry.

```bash
  az acr create -n $REGISTRY -g $RESOURCE_GROUP --sku Basic --admin-enabled true
```

- Go to the root folder of the project. Ensure the full project has been built properly and the jar artifacts are found under the target folder of each subproject.
- Build docker images into Azure Container Registry.

```bash
  cd discovery-service
  az acr build --registry $REGISTRY --image discovery-service:latest .
  cd ../gateway-service
  az acr build --registry $REGISTRY --image gateway-service:latest .
  cd ../product-service
  az acr build --registry $REGISTRY --image product-service:latest .
  cd ../customer-service
  az acr build --registry $REGISTRY --image customer-service:latest .
  cd ../order-service
  az acr build --registry $REGISTRY --image order-service:latest .
```

- Verify that the push was successful listing the images.

```bash
  az acr repository list -n $REGISTRY
```

### Deploy AKS Cluster

_Note:_ Depending on the version of your az cli you may need to install the aks-preview extension to make the aks cluster creation succeed

```bash
az extension add --name aks-preview
```

- Create Azure VNet.

```bash
  az network vnet create --resource-group $RESOURCE_GROUP \
    --name $VNET_NAME \
    --location $REGION \
    --address-prefixes 10.0.0.0/8 \
    --subnet-name $SUBNET_NAME
```

- Create Azure Kubernetes Service instance.

```bash
  az aks create --resource-group $RESOURCE_GROUP \
  --name $AKS_CLUSTER_NAME \
  --vm-set-type VirtualMachineScaleSets \
  --load-balancer-sku standard \
  --location $REGION_NAME \
  --kubernetes-version $(az aks get-versions --location $REGION_NAME --query 'orchestrators[?isPreview] | [-1].orchestratorVersion' --output tsv) \
  --network-plugin azure \
  --vnet-subnet-id $(az network vnet subnet show --resource-group $RESOURCE_GROUP --vnet-name $VNET_NAME --name $SUBNET_NAME --query id -o tsv) \
  --service-cidr 10.2.0.0/24 \
  --dns-service-ip 10.2.0.10 \
  --docker-bridge-address 172.17.0.1/16 \
  --generate-ssh-keys

  az aks update --name $AKS_CLUSTER_NAME --resource-group $RESOURCE_GROUP --attach-acr $REGISTRY
```

- Retrieve your Azure Credencitals.

```bash
  az aks get-credentials --resource-group $RESOURCE_GROUP --name $AKS_CLUSTER_NAME
```

- Deploy your images in the cluster

```bash
  kubectl create deployment discovery-service --image $REGISTRY.azurecr.io/discovery-service:latest
  kubectl create deployment gateway-service --image $REGISTRY.azurecr.io/gateway-service:latest
  kubectl create deployment product-service --image $REGISTRY.azurecr.io/product-service:latest
  kubectl create deployment customer-service --image $REGISTRY.azurecr.io/customer-service:latest
  kubectl create deployment order-service --image $REGISTRY.azurecr.io/order-service:latest

  kubectl expose deployment discovery-service --type LoadBalancer --port $PORT

  kubectl get services
```

- Find the public IP of your service and connect to it using

## Contributing

If you find this repo useful, send a Merge Request with your awesome new features and bug fixes.

## Further Reading / Useful Links

- [Maven](https://maven.apache.org/developers/index.html)
- [Deploy](https://docs.microsoft.com/en-us/azure/app-service/tutorial-custom-container?pivots=container-linux)
