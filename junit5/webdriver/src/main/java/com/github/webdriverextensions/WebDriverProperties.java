/**
 * Copyright © 2017 Sven Ruppert (sven.ruppert@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.webdriverextensions;

//CHECKSTYLE:OFF
public interface WebDriverProperties {
  String CHROME_DRIVER_PROPERTY_NAME  = "webdriver.chrome.driver";
  String FIREFOX_DRIVER_PROPERTY_NAME = "webdriver.gecko.driver";
  String IE_DRIVER_PROPERTY_NAME      = "webdriver.ie.driver";
  String OPERA_DRIVER_PROPERTY_NAME   = "webdriver.opera.driver";
  String EDGE_DRIVER_PROPERTY_NAME    = "webdriver.edge.driver";

  String CHROME_BINARY_PROPERTY_NAME    = "chrome.binary.path";
  String GECKO_BINARY_PROPERTY_NAME     = "gecko.binary.path";
  String IE_BINARY_PROPERTY_NAME        = "ie.binary.path";
  String OPERA_BINARY_PROPERTY_NAME     = "opera.binary.path";
  String EDGE_BINARY_PROPERTY_NAME      = "edge.binary.path";

  String PHANTOMJS_BINARY_PROPERTY_NAME = "phantomjs.binary.path";

  String DISABLED_BROWSERS_PROPERTY_NAME = "webdriverextensions.disabledbrowsers";
}
