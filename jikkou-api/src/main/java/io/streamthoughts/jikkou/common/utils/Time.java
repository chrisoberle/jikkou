/*
 * Copyright 2020 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.common.utils;

public interface Time {

    Time SYSTEM = new SystemTime();

    /**
     * @return the current time in milliseconds.
     */
    long milliseconds();

    /**
     * A time implementation that uses the system clock and sleep call. Use `Time.SYSTEM` instead of creating an instance
     * of this class.
     */
    class SystemTime implements Time {

        /** {@inheritDoc} **/
        @Override
        public long milliseconds() {
            return System.currentTimeMillis();
        }
    }
}