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
export APP_NAME=app-chaosspringboot
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

- Create an Azure App.

```bash
  az webapp create --resource-group $RESOURCE_GROUP \
    --plan $PLAN \
    --name $APP_NAME \
    --deployment-container-image-name $REGISTRY.azurecr.io/$DISCOVERY_SERVICE:latest
```

- Set the WEBSITES_PORT environment variable as expected by Azure App.

```bash
  az webapp config appsettings set
    --resource-group $RESOURCE_GROUP
    --name $APP_NAME
    --settings WEBSITES_PORT=$DISCOVERY_PORT
```

- Enable Azure Managed Identity for the Azure App.

```bash
  az webapp identity assign --resource-group $RESOURCE_GROUP
    --name app-chaspringboot
    --query principalId --output tsv
```

- Retrieve your Azure Subscription ID.

```bash

```

- Grant the web app permission to access the Azure Container Registry.

```bash
  az role assignment create --assignee <principal-id> \
    --scope $(az account show --query id --output tsv) \
    --role "AcrPull"
```

Replace the following values:
<principal-id> with the service principal ID from the az webapp identity assign command
<registry-name> with the name of your container registry
<subscription-id> with the subscription ID retrieved from the az account show command

- Deploy your Azure App!

```bash
  az webapp config container set
    --name app-chaospringboot \
    --resource-group app-chaosspringboot-rg \
    --docker-custom-image-name $REGISTRY.azurecr.io/$DISCOVERY_SERVICE:latest \
    --docker-registry-server-url https://chaosspringbootacr.azurecr.io
```

https://[APP_NAME].azurewebsites.net/

## Contributing

If you find this repo useful, send a Merge Request with your awesome new features and bug fixes.

## Further Reading / Useful Links

- [Maven](https://maven.apache.org/developers/index.html)
- [Deploy](https://docs.microsoft.com/en-us/azure/app-service/tutorial-custom-container?pivots=container-linux)
