/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.useware.kernel.model.structure;

import static org.useware.kernel.mock.TestNamespace.NAMESPACE;
import static org.useware.kernel.model.behaviour.ResourceType.Interaction;
import static org.useware.kernel.model.behaviour.ResourceType.Presentation;
import static org.junit.Assert.*;

import org.useware.kernel.gui.behaviour.Integrity;
import org.useware.kernel.gui.behaviour.IntegrityErrors;
import org.useware.kernel.gui.behaviour.Procedure;
import org.useware.kernel.gui.behaviour.BehaviourMap;
import org.useware.kernel.mock.TestProcedure;
import org.useware.kernel.mock.TestableInteractionUnit;
import org.useware.kernel.model.behaviour.Behaviour;
import org.useware.kernel.model.behaviour.Resource;
import org.useware.kernel.model.behaviour.ResourceType;
import org.useware.kernel.model.structure.builder.Builder;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Test and verify behaviour constraints.
 *
 * @author Heiko Braun
 * @date 10/26/2012
 */
public class BehaviourTest
{
    InteractionUnit unit;

    @Before
    public void setUp()
    {
        this.unit = new TestableInteractionUnit(NAMESPACE, "test", "Test");
    }

    @Test
    public void verifyBehaviourConstraints()
    {

        Input textInput = new Input(NAMESPACE, "firstName", "Firstname");
        Input submit = new Input(NAMESPACE, "submitButton", "Submit");
        Input close = new Input(NAMESPACE, "closeButton", "Close Dialogue");

        InteractionUnit container = new Builder()
                .start(new Container(NAMESPACE, "window", "Window", TemporalOperator.OrderIndependance))
                .add(textInput)
                .add(submit)
                .add(close)
                .end()
                .build();

        assertFalse("Should not produce events by default", submit.doesProduce());

        final Resource<ResourceType> submitEvent = new Resource<ResourceType>(NAMESPACE, "submit", Interaction);
        submit.setOutputs(submitEvent);

        assertTrue("submit should produce events", submit.doesProduce());
        assertFalse("submit should not consume interaction events",
                container.doesConsume(new Resource<ResourceType>(NAMESPACE, "pressCancel", Interaction))
        );


        TestProcedure handleSubmit = new TestProcedure(NAMESPACE, "handleSubmit")
        {
            {
                setInputs(submitEvent);
            }
        };
        assertTrue("Behaviour should be triggered by submitEvent", handleSubmit.doesConsume(submitEvent));

        final Resource<ResourceType> presentationData = new Resource<ResourceType>(NAMESPACE, "data", Presentation);
        TestProcedure resourcePresentation = new TestProcedure(NAMESPACE, "updateDisplay")
        {
            {
                setOutputs(presentationData);
            }
        };
        container.setInputs(presentationData);

        // integrity checks
        final BehaviourMap<TestProcedure> behaviours = new BehaviourMap<TestProcedure>();
        behaviours.add(handleSubmit);
        behaviours.add(resourcePresentation);

        // the integrity check will pass
        try {
            verifyIntegrity(container, behaviours);
        } catch (IntegrityErrors e) {
            throw new AssertionError("Should not raise error: "+e.getMessage());
        }

        // create a derivation that causes the integrity check to fail
        Resource<ResourceType> closeEvent = new Resource<ResourceType>(NAMESPACE, "dialog-close", Interaction);
        close.setOutputs(closeEvent);

        try {
            verifyIntegrity(container, behaviours);
        } catch (IntegrityErrors err) {

            java.lang.System.out.print(err.getMessage());
            // all good, this is expected
        }
    }

    private void verifyIntegrity(InteractionUnit container, BehaviourMap behaviours)
            throws IntegrityErrors {
        Integrity.check(container, behaviours);
    }

    @Test
    public void behaviourResolution()
    {
        final Resource<ResourceType> submitEvent = new Resource<ResourceType>(NAMESPACE, "submitName", Interaction);
        Resource<ResourceType> deviceRotation = new Resource<ResourceType>(NAMESPACE, "deviceRotation", Interaction);
        Resource<ResourceType> loadData = new Resource<ResourceType>(NAMESPACE, "loadData", Interaction);

        Behaviour behaviour = new TestProcedure(NAMESPACE, "onSubmitName")
        {
            {
                setInputs(submitEvent);
            }

        };

        assertTrue("Behaviour can be triggered by submitEvent", behaviour.doesConsume(submitEvent));

        assertFalse("Behaviour should not be triggered by deviceRotation", behaviour.doesConsume(deviceRotation));
        assertFalse("Behaviour should not be triggered by loadData", behaviour.doesConsume(loadData));

    }

    @Test
    public void testProcedureEquality()
    {
        Procedure proc1 = new TestProcedure(QName.valueOf("foo.bar:proc")) {};
        Procedure proc2 = new TestProcedure(QName.valueOf("foo.bar:proc")) {};
        Procedure proc3 = new TestProcedure(QName.valueOf("foo.bar:proc"), QName.valueOf("some:origin")) {};
        Procedure proc4 = new TestProcedure(QName.valueOf("foo.bar:proc2")) {};
        Procedure proc5 = new TestProcedure(QName.valueOf("foo.bar:proc"), QName.valueOf("some:origin")) {};

        assertEquals(proc1, proc2);
        assertFalse(proc2.equals(proc3));
        assertFalse(proc1.equals(proc4));
        assertEquals(proc3, proc5);
    }
}
