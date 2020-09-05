# learning-chaos-springboot

[![azure](https://img.shields.io/badge/azure-yellow.svg)](https://portal.azure.com)
[![azure cli](https://img.shields.io/badge/azurecli-blue.svg)](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest)
[![kubectl](https://img.shields.io/badge/kubectl-red.svg)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

> Instructions to deploy Java Spring Boot Microservices for Chaos Engineering as an Azure APP.
>
> Developed by Chaos Guild Team.

## Prerequisites

You will need the following things properly installed on your computer.

- [Docker](https://www.docker.com/)
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest)
- [Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

## Deploy on Azure

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
export DISCOVERY_APP=discoveryapp
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

- Go to the root folder of the project. Ensure the full project has been built properly and the jar artifacts are found under the target folder of each subproject

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

### Deploy Docker Images

- Create an Azure Plan.

```bash
  az appservice plan create --name $PLAN \
    --resource-group $RESOURCE_GROUP \
    --is-linux
```

- Create an Azure App for each service, next the command for Discovery Service.

```bash
  az webapp create --resource-group $RESOURCE_GROUP \
    --plan $PLAN \
    --name $DISCOVERY_APP \
    --deployment-container-image-name $REGISTRY.azurecr.io/discovery-service:latest

  az webapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $DISCOVERY_APP \
    --settings WEBSITES_PORT=8761
```

- Enable Azure Managed Identity for the Azure App.

```bash
  az webapp identity assign --resource-group $RESOURCE_GROUP \
    --name $DISCOVERY_APP \
    --query principalId --output tsv
```

- Grant the web app permission to access the container registry.

```bash
  export PRINCIPAL_ID="az ad sp list --display-name $DISCOVERY_APP --query '[].objectId' -o tsv"

  export SUBSCRIPTION_ID="az account show --query 'id' -o tsv"

  az role assignment create \
    --assignee $PRINCIPAL_ID \
    --scope /subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.ContainerRegistry/registries/$REGISTRY \
    --role "AcrPull"
```

- Deploy de Image.

```bash
  az webapp config container set \
    --name $DISCOVERY_APP \
    --resource-group $RESOURCE_GROUP \
    --docker-custom-image-name $REGISTRY.azurecr.io/discovery-service:latest \
    --docker-registry-server-url https://$REGISTRY.azurecr.io
```

- Access in a browser using the proper URL
  https://discoveryapp.azurewebsites.net/

## Contributing

If you find this repo useful, send a Merge Request with your awesome new features and bug fixes.

## Further Reading / Useful Links

- [Maven](https://maven.apache.org/developers/index.html)
- [Deploy](https://docs.microsoft.com/en-us/azure/app-service/tutorial-custom-container?pivots=container-linux)

```

```

```

```
