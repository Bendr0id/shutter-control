package com.vidi.shuttercontrol;

import com.pi4j.io.gpio.*;

import org.apache.commons.cli.*;

import java.util.*;

public class ShutterControl
{
    private static String HEADER = "ShutterControl 1.0";
    private static String USAGE = "[-h] -s <shutter> -o <operation>";

    private static String OPERATION_SELECT = "SELECT";
    private static String OPERATION_UP = "UP";
    private static String OPERATION_DOWN = "DOWN";
    private static String OPERATION_STOP = "STOP";

    private int shutter;
    private String operation;

    private GpioController gpio;
    private Map<String, GpioPinDigitalOutput> buttons;
    private Map<Integer, GpioPinDigitalInput> leds;

    public ShutterControl(String[] args) throws InterruptedException
    {
        parseCommandLine(args);

        System.out.println("Start.");

        gpio = GpioFactory.getInstance();

        buttons = new HashMap<String, GpioPinDigitalOutput>();

        addButton(RaspiPin.GPIO_00, OPERATION_SELECT);
        addButton(RaspiPin.GPIO_01, OPERATION_UP);
        addButton(RaspiPin.GPIO_02, OPERATION_DOWN);
        addButton(RaspiPin.GPIO_03, OPERATION_STOP);

        leds = new HashMap<Integer, GpioPinDigitalInput>();
        addLed(RaspiPin.GPIO_04, 1);
        addLed(RaspiPin.GPIO_05, 2);
        addLed(RaspiPin.GPIO_06, 3);
        addLed(RaspiPin.GPIO_07, 4);

        selectShutter(shutter);
        pushButton(buttons.get(operation));

        System.out.println("End.");

        gpio.shutdown();
    }

    public static void main(String[] args) throws InterruptedException
    {
        new ShutterControl(args);
    }

    private void addButton(Pin pin, String operation)
    {
        GpioPinDigitalOutput button = gpio.provisionDigitalOutputPin(pin, operation, PinState.LOW);
        button.setShutdownOptions(true, PinState.LOW);

        buttons.put(operation, button);
    }

    private void addLed(Pin pin, int number)
    {
        leds.put(number, gpio.provisionDigitalInputPin(pin, "LED" + number, PinPullResistance.PULL_DOWN));
    }

    private void selectShutter(int shutter) throws InterruptedException
    {
        System.out.println("selectShutter(" + shutter + ")");

        while (!leds.get(shutter).isHigh() || allHigh(leds.values()))
        {
            pushButton(buttons.get(OPERATION_SELECT));
        }
    }

    private void pushButton(GpioPinDigitalOutput button) throws InterruptedException
    {
        System.out.println("pushButton(" + button.getName() + " [" + button.getPin() + "])");
        button.pulse(500);

        Thread.sleep(1000);
    }

    private boolean allHigh(Collection<GpioPinDigitalInput> leds)
    {
        boolean state = true;

        for (GpioPinDigitalInput led : leds)
        {
            state &= led.isHigh();
        }

        return state;
    }

    private void parseCommandLine(String args[])
    {
        Options options = getOptions();

        try
        {
            CommandLineParser cmdParser = new BasicParser();
            CommandLine commandLine = cmdParser.parse(options, args);

            if (commandLine.hasOption('h'))
            {
                printUsage(options);
                System.exit(0);
            }

            shutter = Integer.valueOf(commandLine.getOptionValue('s'));
            operation = commandLine.getOptionValue('o').toUpperCase();

            if (shutter <= 0 || shutter > 4)
            {
                throw new Exception("Invalid shutter number. possible is 1-4.");
            }

            if (!operation.equals(OPERATION_UP) &&
                !operation.equals(OPERATION_DOWN) &&
                !operation.equals(OPERATION_STOP))
            {
                throw new Exception("Invalid operation. possible is up/down/stop.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println();

            printUsage(options);
            System.exit(1);
        }
    }

    private Options getOptions()
    {
        Options options = new Options();

        options.addOption(OptionBuilder.hasArg(true)
            .withType(Integer.class)
            .withArgName("1/2/3/4")
            .withLongOpt("shutter")
            .isRequired()
            .create('s'));

        options.addOption(OptionBuilder.hasArg(true)
            .withType(String.class)
            .withArgName("up/down/stop")
            .withLongOpt("operation")
            .isRequired()
            .create('o'));

        return options;
    }

    private void printUsage(Options options)
    {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(140);
        helpFormatter.printHelp(USAGE, HEADER, options, null);
    }
}