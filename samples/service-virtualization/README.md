# Service Virtualization Environment

As part of the [HPE OneView Global Dashboard](https://www.hpe.com/us/en/product-catalog/detail/pip.1009187269.html) project, an environment to simulate services has been developed. This environment has the following goals:
* Allow developers to simulate a service that is not yet completed (allowing them to stay productive);
* Build a stable and scalable integration testing environment;
* Deploy as many services as needed to execute the application scaling tests.

## Technologies
Below are the technologies used to build the virtualization environment:
* [Dnsmasq](http://www.thekelleys.org.uk/dnsmasq/doc.html)
  * Lightweight service that provides network infrastructure functionalities such as DNS and DHCP.
  * Specify an IP address for a given domain: https://wiki.archlinux.org/index.php/dnsmasq#Override_addresses
* [Docker](https://www.docker.com/)
  * Platform that provides a way to run application isolated in a container.
  * Docker run reference: https://docs.docker.com/engine/reference/run/
* [NGINX](https://www.nginx.com/)
  * Open source web server and reverse proxy.
  * Passing a request to a proxied server: https://www.nginx.com/resources/admin-guide/reverse-proxy/
  * Regular expressions for server names: http://nginx.org/en/docs/http/server_names.html#regex_names
  * SSL configuration: https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-16-04
* [WireMock](http://wiremock.org/)
  * HTTP mock server tool that can serve pre-defined responses to particular requests.
  * Standalone process: http://wiremock.org/docs/running-standalone/
  * Running inside a Docker container: https://github.com/lhsvobodaj/docker-wiremock

## Overall Environment Architecture

The virtualization environment is composed of a virtual machine running Ubuntu Server 16.04 with Dnsmasq and Docker services running as native processes, while the other tools that complete the setup (NGINX and WireMock) run as containers.

* Services running on the host VM
  * Dnsmasq: provides DNS capabilities.
  * Docker: allows the environment to scale.

* Services running as containers
  * NGINX: configured as a reverse proxy.
    ```bash
    docker run -d -v /root/nginx/certs:/etc/nginx/certs \
      -v /root/nginx/conf.d:/etc/nginx/conf.d --name nginx --network=host nginx:1.13.5
    ```
  * WireMock: allows the virtualization of any REST service.
    ```bash
    docker run -d -v /root/wiremock/cluster-8443:/opt/wiremock/cluster-8443 \
    --network=host --name=cluster-8443 lhsvobodaj/wiremock \
    --root-dir=/opt/wiremock/cluster-8443 --port=8080 --https-port=8443
    ```

![Overall Architecture](img/environment-architecture.png)

## Use Case #1 - Integration Test Environment
The idea is to have a stable environment to avoid spurious failures when running the application integration tests.

The corresponding NGINX configuration to run enable this use case is as follows:
```nginx
server {
    listen 443 ssl;
    server_name "~^host-(?<port>\d{4}).oneview\.ovgd$";
    ssl_certificate /etc/nginx/certs/nginx.crt;
    ssl_certificate_key /etc/nginx/certs/nginx.key;
    location / {
        proxy_pass https://127.0.0.1:$port;
    }
}
```

## Use Case #2 - Scale Environment

The OneView Global Dashboard application has some upper limits regarding the number of appliances it can manage. In order to test these limits, some WireMock containers are started containing mock files obtained recording the interaction between the OVGD and a deployed appliance. Combined with the extension to replace the original resource identifiers, it is possible to simulated the necessary environment. Another important aspect of this environment is the amount of computational resources consumed to deploy it. When using deployed VMs, a large amount of resources are consumed (CPU, memory and disk). Although we can not precisely identify how much is consumed when simulating the appliances with containers, it is clear that we drastically drop the amount of resources in order to run an equivalent environment.

![OneView VM](img/oneview-vm-cpu100.png)

## Use Case #3 - SimpliVity Support Development

### Request Routing
Using NGINX as a reverse proxy, it is possible to send the HTTP request to a specific proxied server. In this case, the request is routed according to the request hostname. The domain name chosen to host our mocked environment is **simplivity.ovgd**. Thus, the `server_name` field in the NGINX configuration file is defined using a regular expression as `cluster-<PORT>-host<ID>.simplivity.ovgd`.

According to the provided hostname, the request is routed to the server running on the port defined by the `<PORT>` portion of the address. See the NGINX virtual server configuration below:
```nginx
server {
    listen 443 ssl;
    server_name "~^cluster-(?<port>\d{4})-host\d{1}\.simplivity\.ovgd$";
    ssl_certificate /etc/nginx/certs/nginx.crt;
    ssl_certificate_key /etc/nginx/certs/nginx.key;
    location / {
        proxy_pass https://127.0.0.1:$port;
    }
}
```
![Request Routing](img/nginx-routing.png)

### How to use the environment?
* Add the VM IP address as a DNS server in your machine (or on the machine that needs to access this environment);
* If you need to simulate a new SimpliVity cluster, copy the WireMock folders `mappings` and `__files` to the VM and place them inside a common directory;
* Start a new WireMock container using the just created directory as the WireMock root dir (`--root-dir`);
> Pay attention to use a different HTTP/HTTPS port(s).

Access your new SimpliVity cluster using the hostname **cluster-WXYZ-host1.simplivity.ovgd**.