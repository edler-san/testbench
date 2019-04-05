package junit.com.vaadin.testbench.tests.component.formlayout;

import com.vaadin.flow.component.formlayout.testbench.FormLayoutElement;
import com.vaadin.flow.component.formlayout.testbench.test.FormLayoutView;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.addons.junit5.extensions.unittest.VaadinTest;
import junit.com.vaadin.testbench.tests.component.common.AbstractIT;
import junit.com.vaadin.testbench.tests.testUI.GenericTestPageObject;
import org.junit.jupiter.api.Assertions;

import static com.vaadin.flow.component.formlayout.testbench.test.FormLayoutView.NAV;

@VaadinTest
public class FormLayoutIT extends AbstractIT {

    @VaadinTest(navigateAsString = NAV)
    public void findInside(GenericTestPageObject po) throws Exception {
        final FormLayoutElement formLayout = po.$(FormLayoutElement.class).id(FormLayoutView.DEFAULT);

        Assertions.assertEquals(3, po.$(TextFieldElement.class).all().size());
        Assertions.assertEquals(2,
                formLayout.$(TextFieldElement.class).all().size());
    }

}