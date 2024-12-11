
import java.util.*;

class Component {
    String name;
    int number; // Number of this component required by its parent
    List<Component> components;

    public Component(String name, int number) {
        this.name = name;
        this.number = number;
        this.components = new ArrayList<>();
    }

    public void addComponent(Component component) {
        components.add(component);
    }
}

public class HierachicalComponents {
    public static int findComponents(Component input, Component output) {
        int count = 0;
        // If output can be made from input component
        if (input.name.equals(output.name)) {
            return input.number;
        }
        // If input is a leaf component
        if (input.components == null || input.components.isEmpty()) {
            return 0;
        }
        // Iterate the list of sub-components
        for (Component temp : input.components) {
            // See how many output components can be made from temp
            count += findComponents(temp, output);
        }
        return count * input.number;
    }

    public static void main(String[] args) {
        // Build the component hierarchy for Bike
        Component bike = new Component("Bike", 1); // Top-level component

        // Engine and its components
        Component engine = new Component("Engine", 1); // Bike requires 1 Engine
        Component nutEngine = new Component("Nuts", 400); // Engine requires 400 Nuts
        engine.addComponent(nutEngine);
        // ... add other engine components if necessary

        // Wheels and their components
        Component wheel = new Component("Wheels", 2); // Bike requires 2 Wheels
        Component nutWheel = new Component("Nuts", 50); // Each Wheel requires 50 Nuts
        wheel.addComponent(nutWheel);
        // ... add other wheel components if necessary

        // Assemble the Bike
        bike.addComponent(engine);
        bike.addComponent(wheel);
        // ... add other bike components if necessary

        // Create output Components (number can be any value as it's not used in comparison)
        Component outputWheels = new Component("Wheels", 0);
        Component outputNuts = new Component("Nuts", 0);
        Component outputBike = new Component("Bike", 0);

        // Test Case 1: Bike requires 2 Wheels
        int numWheels = findComponents(bike, outputWheels);
        System.out.println("Input: Bike, Wheels");
        System.out.println("Output: " + numWheels);
        System.out.println(numWheels + " wheels are required in the process of manufacturing the Bike\n");

        // Test Case 2: Bike requires 500 Nuts
        int numNuts = findComponents(bike, outputNuts);
        System.out.println("Input: Bike, Nuts");
        System.out.println("Output: " + numNuts);
        System.out.println(numNuts + " nuts are required in the process of manufacturing the Bike\n");

        // Test Case 3: Nuts do not require any Bikes to manufacture
        Component nuts = new Component("Nuts", 1); // Assuming a Nut is a basic component
        int numBikes = findComponents(nuts, outputBike);
        System.out.println("Input: Nuts, Bike");
        System.out.println("Output: " + numBikes);
        System.out.println("As no Bikes are required in the process of manufacturing the Nut.");
    }
}
/*
There is a manufacturer who manufactures bikes, cars, washing machines, AC's etc. Bikes contain Engine, Gas Tank, Seat,
 Hand clutch, Brake Rod, Oil tank, Foot Rest, Starter Pedal, Rear Wheel, Exhaust pipes, Brake cable, Headlight, speedometer,
  Horn, Exhaust Pipe, Mirrors, License plate, Tail Light and each of the above part is an complex structure For e.g.
  Engine contains screws, nuts, oil feed pipe, Inlet pipe, oil pump, cylinder head, exhaust post, cylinder head, cooling
  fin, piston etc.. Same goes with cars, washing machine, AC's etc..


By the time he explained all the machines from bikes to AC's and all the parts and sub parts and sub sub parts...,
I am going mad and gave a smile. After which he stopped explaining these parts and sub parts stuff And asked me to write
a program which takes two components as input and return how many components of second argument are required to make
first component. I know by the time you read this you are going crazy, Let me give you an example to explain what
I need to code exactly.


Example :


Input : Bike, Wheels
Output : 2
2 wheels are required in the process of manufacturing the Bike


Input : Bike, Nuts
Output : 500
500 nuts are required in the process of manufacturing the Bike


Input : Nuts, Bike
Output : 0
As no Bikes are required in the process of manufacturing the Nut.


Note : Your code should be very generic. So that if manufacturer manufactures a new product ten years down the line
let's say cooler, he should not come back to you for any modifications in your function.


To be honest, I am blank for the first 20 mins and then suddenly an Idea sparked and I started implementing it using
trees. I was not sure on how to represent the data in a generic way. First he asked me about the Data Structure on how
I am representing the data. Once I have shown him my Component class(See Below) he seems to be convinced and asked me
to Code the logic. Once I am done with my coding he gave some test cases and asked to check what my program returns.
Attaching a raw template of the code below
 */
