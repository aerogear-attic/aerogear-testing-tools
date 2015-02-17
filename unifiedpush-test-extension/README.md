# UnifiedPush Server Test Extension

Purpose of this project is to make UnifiedPush Server testable in any environment. It adds some extra endpoints which are not present in the UnifiedPush Server itself. It also automatically creates a proxy to allow push service simulation.

NOTE: The proxy behavior is not yet working. Currently only `http` and `https` requests can go through the proxy and we only log each request. In the final state it should simulate the `APNS`, `GCM` and `SimplePush` services and save the information about the push messages so they can be requested in our test suite.

## How to build

You need to clone and install LittleProxy prior to the installation of this extension itself. You need 1.1.0.Beta1-SNAPSHOT version of LittleProxy from here https://github.com/adamfisk/LittleProxy.

All you need is to invoke the following command:

```
mvn clean package
```

This command will create a war file and remove `persistence.xml` file from the keycloak dependency jar.

Afterwards you will find `unifiedpush-test-extension-server.war` file in the `unifiedpush-test-extension-server/target` directory.

## How to deploy

### WildFly, AS or EAP

Simply copy the `unifiedpush-test-extension-server.war` file into `$JBOSS_HOME/standalone/deployments/` directory.

### Openshift

Deployment to OpenShift can be done either directly using `sftp` or by pushing into the git repository or via cli tool `upte`.

* #### SFTP upload
  Connect to your application and upload the `unifiedpush-test-extension.war` file into `~/ag-push/`

* #### Push to git repository

  1. Clone your application's git repository
  2. Add the `unifiedpush-test-extension.war` file into the root of the repository.
  3. Create file named `deploy` in `$REPOSITORY/.openshift/action_hooks` and add following content:

    ```sh
    mv ~/app-root/repo/unifiedpush-test-extension.war ~/ag-push/standalone/deployments/unifiedpush-test-extension.war
    ```

  4. Commit and push

  After these steps, the server should restart and try deploying the test extension.
  
* #### CLI tool `upte`

  1. Navigate to `unifiedpush-test-extension-client/target` directory.
  2. There is tool `$./upte app-create` which can be used for create UPS cartridge with test extension, eg.
  
     ```sh
     ./upte app-create --app-name foo --namespace mobileqa --force
     ```
     
     (use help command for more details `.\upte help`)

## How to use

### Proxy

To simulate push services like `APNS` and `GCM`, we bundled a proxy into the test extension. It gets started automatically when you deploy the war file and by default it uses port `16000`. You need to do one more thing to make sure the `http` and `https` communication goes through the proxy and that is running the following command:

```sh
rhc set-env JAVA_OPTS_EXT="-Dhttp.proxyHost={application ip} -Dhttp.proxyPort=16000 -Dhttps.proxyHost={application ip} -Dhttps.proxyPort=16000" -a {application name}
```

Where to find the `application ip`?

* You can find out the `application ip` from the gear by using command `env` and going through the environment properties. One of them should have the `IP` in its name and its value should be the internal IP of the gear.

* Or you can check the log from the test extension deployment. It should contain `Starting proxy on {ip}:{port}`.

// TODO: Once the socket proxy is working, add the `-DsocksProxyHost=127.6.224.1 -DsocksProxyPort=16001` into the command above.

### KeyCloak configurator

By default, UnifiedPush Server does not allow REST login. We exposed the `/keycloak` endpoint so that when you need to login using REST, you just access the `/keycloak` first and we will reconfigure the KeyCloak in a way that it allows REST authorization and does not require password change (which is also not possible with REST).

### Proxy activation

There is embedded proxy server in order to be able to route notifications to mocked server for further inspection. This proxy server setup is turned off by default. You have to turn it
on explicitly, otherwise messages will be sent to real notification providers.

Turning proxy on is done by calling REST endpoint `/proxy/activate`. Deactivation is done by calling `/proxy/deactivate`.

### Data generator

For example: You want to add 10000 applications, for each application there will be 10 variants. You want 
to simulate installation of 125000 devices. Each device will be in 50 categories and you 
are choosing these categories randomly from 1000 categories totally.

```sh
./upte generate-data --app-name foo \ 
    --applications 100000 \
    --categories 1000 \
    --categories-per-installation 50 \ 
    --installations 125000 \ 
    --installation-distribution PARETO \ 
    --variants 10 \ 
    --variant-type ANDROID \ 
    --variant-distribution EQUAL \ 
    --cleanup-database
```

(use help command for more details `.\upte help`)
