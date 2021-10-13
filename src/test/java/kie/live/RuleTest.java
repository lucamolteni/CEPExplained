/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kie.live;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleTest {

    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);

    @Test
    public void testUsingSystemClock() throws InterruptedException {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        KieBase kieBase = kContainer.getKieBase("CEPExplained");
        KieSession session = kieBase.newKieSession();

        Deque<HeartBeat> check = new ArrayDeque<>();
        session.setGlobal("controlSet", check);

        HeartBeat hb1 = new HeartBeat();
        Date hb1Date = Date.from(Instant.now());
        hb1.setTs(hb1Date);

        session.insert(hb1);

        // You shouldn't probably test like this
        Thread.sleep(6000);

        session.fireAllRules();

        assertEquals(hb1, check.pop());
    }

    @Test
    public void testUsingPseudoClock() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kContainer = kieServices.getKieClasspathContainer();
        KieBase kieBase = kContainer.getKieBase("CEPExplained");

        KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();

        conf.setOption(ClockTypeOption.PSEUDO);
        KieSession session = kieBase.newKieSession(conf, null);

        Deque<HeartBeat> check = new ArrayDeque<>();
        session.setGlobal("controlSet", check);

        SessionPseudoClock clock = session.getSessionClock();

        HeartBeat hb1 = new HeartBeat();
        Date hb1Date = tsFromPseudoClock(clock);
        hb1.setTs(hb1Date);

        session.insert(hb1);
        clock.advanceTime(5, TimeUnit.SECONDS);

        session.fireAllRules();

        assertEquals(hb1, check.pop());
    }

    @Test
    public void testSystemReboot() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kContainer = kieServices.getKieClasspathContainer();
        KieBase kieBase = kContainer.getKieBase("CEPExplained");

        KieSessionConfiguration conf = KieServices.Factory.get().newKieSessionConfiguration();

        conf.setOption(ClockTypeOption.PSEUDO);
        KieSession session = kieBase.newKieSession(conf, null);

        Deque<User> usersNotified = new ArrayDeque<>();
        session.setGlobal("usersNotified", usersNotified);

        Deque<Long> check = new ArrayDeque<>();
        session.setGlobal("controlSet", check);

        SessionPseudoClock clock = session.getSessionClock();


        session.insert(new User(333L, "luca"));

        session.insert(new SystemRebootEvent(tsFromPseudoClock(clock),
                                             10000L, 1L, 333L));

        clock.advanceTime(20, TimeUnit.MINUTES);

        session.insert(new SystemRebootEvent(tsFromPseudoClock(clock),
                                             10000L, 1L, 333L));

        clock.advanceTime(10, TimeUnit.MINUTES);

        session.insert(new SystemRebootEvent(tsFromPseudoClock(clock),
                                             10000L, 1L, 333L));

        session.fireAllRules();

        User userNotified = usersNotified.pop();
        assertEquals("luca", userNotified.getUsername());
        assertTrue(userNotified.isNotified());
    }

    private Date tsFromPseudoClock(SessionPseudoClock clock) {
        return Date.from(Instant.ofEpochMilli(clock.getCurrentTime()));
    }
}