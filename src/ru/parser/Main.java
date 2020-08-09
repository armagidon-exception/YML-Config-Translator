package ru.parser;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Random;
import java.util.Set;

public class Main
{
    public static void main(String[] args) throws IOException {
        if (args.length != 3) return;
        String inputFileName = args[0];
        String outputFileName = args[1];
        String language = args[2];
        File inputFile = new File(inputFileName);
        if (!inputFile.exists()) {
            System.out.println("This file doesnt exist");
            return;
        }

        File outputFile = new File(outputFileName);
        if(!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Translator translator = (Translator) loadTranslator(new File("gtranslateapi-1.0.jar"));

        YamlConfiguration input = (YamlConfiguration) loadYaml(new File("spigot-1.16.1.jar"),inputFile);
        YamlConfiguration output = (YamlConfiguration) loadYaml(new File("spigot-1.16.1.jar"),outputFile);
        if(input==null||output==null){
            System.out.println("Yaml not found!");
            return;
        }

        Set<String> keys = ImmutableSet.copyOf(input.getKeys(true));
        keys.forEach(key->{
            String value = input.getString(key);
            value = ChatColor.translateAlternateColorCodes('&',value);
            value = ChatColor.stripColor(value);
            try {
                System.out.println(value);
                output.set(key, getTranslatedText(value, "en",language));
                System.out.println(output.get(key));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        try {
            output.save(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object loadYaml(File lib, File toLoad){
        try {
            URLClassLoader child = new URLClassLoader(
                    new URL[]{lib.toURI().toURL()},
                    Main.class.getClassLoader()
            );
            Class<?> classToLoad = Class.forName("org.bukkit.configuration.file.YamlConfiguration", true, child);
            Method method = classToLoad.getDeclaredMethod("loadConfiguration",File.class);
            return method.invoke(null,toLoad);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String getTranslatedText(String input, String sl, String tl) throws IOException {

        String url = "https://api.mymemory.translated.net/get?q="+URLEncoder.encode(input, "UTF-8")+"&langpair="+sl+"|"+tl;
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
        urlConnection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
        reader.close();
        JsonObject response = obj.get("responseData").getAsJsonObject();
        return response.get("translatedText").getAsString();
    }

    private static Proxy getProxy(){
        String url = "https://api.proxyscrape.com/?request=displayproxies&proxytype=http&country=all&anonymity=all&ssl=yes&timeout=2000";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));
            String[] lines = reader.lines().toArray(String[]::new);
            int index = new Random().nextInt(lines.length-1);
            String[] address = lines[index].split(":");
            System.out.println(lines[index]);
            String ip =address[0];
            int port = Integer.parseInt(address[1]);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip,port));
            reader.close();
            return proxy;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
