# API Processing Engine

Detects API error responses, runs one or more sets of corrective calls, and retries the request.

## How it works

APE sits in Burp's HTTP handler. For every request from an enabled tool (Repeater and Intruder by
default), it sends the request and checks the response before Burp sees it.

1. **Error conditions** describe what a failure looks like: a status code, text in the body, or a
   header value, in the request or the response.
2. **Trigger conditions** combine error conditions with AND, OR and NOT, and each one points to a
   corrective call set. They are checked in order, and only the first match fires.
3. **Corrective call set:** the matched set's calls are sent in order, for example a login call.
   Values can be extracted from any of these responses, or from the original error response, and
   inserted into later calls.
4. **Followup call:** each set ends with a followup, a retry of the original request with extracted
   values swapped in, such as a fresh session token.
5. The followup's response is returned to Burp in place of the original error. If nothing matches,
   or the followup gets no response, the original response is returned unchanged.

Every step uses Burp's own networking, so upstream proxies and other Burp settings still apply.

## Usage

1. In Repeater, right-click the HTTP request that returned the error response you want to handle.
   In the context menu, click **Extensions > APE > Send to Error Detection**.

2. Define an error condition. It can be a status code, a value within the body of the response
   (or request), or the value of a specific header.
   Optional: you may extract text from either the request or the response to use in subsequent calls.

3. Optional: in Repeater, right-click a call that will help correct the error and click
   **Extensions > APE > Send to Corrective Calls**. You may extract or replace text in these calls,
   and you may define multiple sets of corrective calls.

4. Optional: replace any text in the followup call if needed. The followup call is a retry of the
   original errored call.

5. Define a trigger condition. A trigger condition associates a set of corrective calls with one or
   more error conditions. Trigger conditions are evaluated in order, and the first one that evaluates
   as true (its error condition is detected) sends its set of corrective calls. Only one trigger
   condition may run per HTTP request.

6. Press the **Test** button to see the individual steps.

By default, API Processing Engine works on all Repeater and Intruder requests. You can choose which
tools APE works with in the **Settings** tab.

## Building

Requires Java 21 or newer. Gradle itself is not needed; the included wrapper downloads it.

```
./gradlew jar
```

The extension is written to `build/libs/APE.jar`. In Burp, go to **Extensions > Add**, choose
**Java**, and select that file.

The only dependency is the Montoya API, which Gradle downloads from Maven Central on the first build.

## Testing

[Test-API](https://github.com/Segue-1/Test-API) is a small local API built for trying APE out. Each of
its endpoints fails in a predictable way (expired sessions, version conflicts, rate limits, maintenance
mode), and its README walks through example test cases.

