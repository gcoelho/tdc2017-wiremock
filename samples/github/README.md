# GitHub API mocking example

In this example we will use WireMock to record and playback a few GitHub REST APIs.

## GitHub API
We will use three endpoints as follow. For more information check https://developer.github.com/v3/.

* GET api.github.com/repos/:owner/:repo
* GET api.github.com/repos/:owner/:repo/commits
* GET api.github.com/repos/:owner/:repo/issues

You can use *curl* or any other application to query these endpoints. No authentication is required.

Examples using curl

```bash
curl -i -X GET https://api.github.com/repos/gcoelho/tdc2017-wiremock
curl -i -X GET https://api.github.com/repos/gcoelho/tdc2017-wiremock/commits
curl -i -X GET https://api.github.com/repos/gcoelho/tdc2017-wiremock/issues
```

## Recording
WireMock has different options to start recording: an UI, a Java API and a REST API. Check http://wiremock.org/docs/record-playback/ for more details. In this example we will use the UI option.

Start WireMock recorder UI using

```bash
 java -jar wiremock-standalone-2.10.1.jar
```
>Flag *--verbose* can help you troubleshooting issues while recording

>If your network uses proxies then add *--proxy-via* flag to allow WireMock to access the Internet

Open a browser on http://localhost:8080/__admin/recorder and enter the hostname base URL you want to record in the "Target URL" field (don't use REST endpoints here). Click "Record" button to start recording. Once finished, click on "Stop" to stop recording and have the mock/stub files saved.

Open a terminal and use curl to query GitHub APIs. Notice that you must point curl to WireMock, not GitHub, so it can record your requests. WireMock will forward requests and return responses to you. In this example, curl would look like this

```bash
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock/commits
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock/issues
```
>WireMock recorder uses port 8080 by default and does not enable HTTPS/SSL.

Mocks are saved on a subfolder of WireMock named 'mappings'. These are JSON files you can open on any text editor.

## Playback
Start WireMock as usual to playback these mocks.

```bash
 java -jar wiremock-standalone-2.10.1.jar --verbose
```

Now using curl you can query your local WireMock and it will behave just like GitHub API server.

```bash
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock/commits
curl -i -X GET http://localhost:8080/repos/gcoelho/tdc2017-wiremock/issues
```
>Because we started WireMock with --verbose flag then you will be able to see it receiving your request and matching the appropriated response.
