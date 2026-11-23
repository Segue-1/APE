# API Processing Engine

Detects API error responses, runs one or more sets of corrective calls, and retries the request.

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

