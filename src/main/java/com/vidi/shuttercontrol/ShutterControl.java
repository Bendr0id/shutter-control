package com.vidi.shuttercontrol;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShutterControl
{
    private final static String HEADER = "ShutterControl 1.0";
    private final static String USAGE = "[-h] -s <shutter> -o <operation>";

    private final static String OPERATION_SELECT = "SELECT";
    private final static String OPERATION_UP = "UP";
    private final static String OPERATION_DOWN = "DOWN";
    private final static String OPERATION_STOP = "STOP";

    private int shutter;
    private String operation;

    private GpioController gpio;
    private Map<String, GpioPinDigitalOutput> buttons;
    private Map<Integer, GpioPinDigitalInput> leds;

    public static void main(String[] args) throws InterruptedException
    {
        new ShutterControl(args);
    }

    public ShutterControl(String[] args) throws InterruptedException
    {
        parseCommandLine(args);

        gpio = GpioFactory.getInstance();

        defineAllButtons();
        defineAllLeds();

        selectShutter(shutter);

        pushButton(buttons.get(operation));

        gpio.shutdown();
    }

    private void defineAllLeds()
    {
        leds = new HashMap<Integer, GpioPinDigitalInput>();
        defineLed(RaspiPin.GPIO_04, 1);
        defineLed(RaspiPin.GPIO_05, 2);
        defineLed(RaspiPin.GPIO_06, 3);
        defineLed(RaspiPin.GPIO_07, 4);
    }

    private void defineAllButtons()
    {
        buttons = new HashMap<String, GpioPinDigitalOutput>();
        defineButton(RaspiPin.GPIO_00, OPERATION_SELECT);
        defineButton(RaspiPin.GPIO_01, OPERATION_UP);
        defineButton(RaspiPin.GPIO_02, OPERATION_DOWN);
        defineButton(RaspiPin.GPIO_03, OPERATION_STOP);
    }

    private void defineButton(Pin pin, String operation)
    {
        GpioPinDigitalOutput button = gpio.provisionDigitalOutputPin(pin, operation, PinState.LOW);
        button.setShutdownOptions(true, PinState.LOW);

        buttons.put(operation, button);
    }

    private void defineLed(Pin pin, int number)
    {
        leds.put(number, gpio.provisionDigitalInputPin(pin, "LED" + number, PinPullResistance.PULL_DOWN));
    }

    private void selectShutter(int shutter) throws InterruptedException
    {
        System.out.println("selectShutter(" + shutter + ")");

        while (!isLedOn(shutter) || isEveryLedOn(leds.values()))
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

    private boolean isLedOn(int index)
    {
        return leds.get(index).isHigh();
    }

    private boolean isEveryLedOn(Collection<GpioPinDigitalInput> leds)
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

            validateShutterParameter();
            validateOperationParameter();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println();

            printUsage(options);
            System.exit(1);
        }
    }

    private void validateShutterParameter() throws Exception
    {
        if (shutter <= 0 || shutter > 4)
        {
            throw new Exception("Invalid shutter number. possible is 1-4.");
        }
    }

    private void validateOperationParameter() throws Exception
    {
        if (!operation.equals(OPERATION_UP) &&
            !operation.equals(OPERATION_DOWN) &&
            !operation.equals(OPERATION_STOP))
        {
            throw new Exception("Invalid operation. possible is up/down/stop.");
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