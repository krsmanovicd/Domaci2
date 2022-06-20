package pkg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Average {
	public static class AverageTuple implements Writable {

        private int minimalnaSuma = 0;
        private int brojacMin = 0;
        private int maksimalnaSuma = 0;
        private int brojacMax = 0;

        public int getMinimalnaSuma() {
            return minimalnaSuma;
        }

        public void setMinimalnaSuma(int minimalnaSuma) {
            this.minimalnaSuma = minimalnaSuma;
        }

        public int getbrojacMin() {
            return brojacMin;
        }

        public void setbrojacMin(int brojacMin) {
            this.brojacMin = brojacMin;
        }

        public int getMaksimalnaSuma() {
            return maksimalnaSuma;
        }

        public void setMaksimalnaSuma(int maksimalnaSuma) {
            this.maksimalnaSuma = maksimalnaSuma;
        }

        public int getbrojacMax() {
            return brojacMax;
        }

        public void setbrojacMax(int brojacMax) {
            this.brojacMax = brojacMax;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            minimalnaSuma = in.readInt();
            brojacMin = in.readInt();
            maksimalnaSuma = in.readInt();
            brojacMax = in.readInt();
        }
        @Override
        public void write(DataOutput out) throws IOException {
            out.writeInt(minimalnaSuma);
            out.writeInt(brojacMin);
            out.writeInt(maksimalnaSuma);
            out.writeInt(brojacMax);
        }

        public String toString() {
            return "ProsekMinimalneTemperature: " + (1.0 * minimalnaSuma/brojacMin) + ", ProsekMaksimalneTemperature: " + (1.0 * maksimalnaSuma/brojacMax);
        }

    }


    public static class TemperatureMapper extends Mapper<Object, Text, Text, AverageTuple> {
        private Text mjesec = new Text();
        private AverageTuple outTuple = new AverageTuple();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split(",");
            mjesec.set(line[1].substring(4,6));
            int temperatura = Integer.parseInt(line[3]);

            String upitT = line[2];
            if(upitT.equals("TMIN")){
                outTuple.setMinimalnaSuma(temperatura);
                outTuple.setbrojacMin(1);
            }else if(upitT.equals("TMAX")){
                outTuple.setMaksimalnaSuma(temperatura);
                outTuple.setbrojacMax(1);
            }

            context.write(mjesec, outTuple);
        }
    }

    public static class TemperatureReducer extends Reducer<Text, AverageTuple, Text, AverageTuple> {

        private AverageTuple resultTuple = new AverageTuple();

        public void reduce(Text key, Iterable<AverageTuple> tuples, Context context) throws IOException, InterruptedException {
        	
            int minimalnaSuma = 0;
            int maksimalnaSuma = 0;
            int minimalnaBrojac = 0;
            int maksimalnaBrojac = 0;

            for(AverageTuple tup : tuples){
            	minimalnaSuma += tup.getMinimalnaSuma();
            	maksimalnaSuma += tup.getMaksimalnaSuma();
            	minimalnaBrojac += tup.getbrojacMin();
            	maksimalnaBrojac += tup.getbrojacMax();
            }

            resultTuple.setMinimalnaSuma(minimalnaSuma);;
            resultTuple.setbrojacMin(minimalnaBrojac);
            resultTuple.setMaksimalnaSuma(maksimalnaSuma);;
            resultTuple.setbrojacMax(maksimalnaBrojac);

            context.write(key, resultTuple);
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "prosecna temperatura");
        job.setJarByClass(Average.class);
        job.setMapperClass(TemperatureMapper.class);
        job.setCombinerClass(TemperatureReducer.class);
        job.setReducerClass(TemperatureReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AverageTuple.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job,  new Path(args[2]));
        System.exit(job.waitForCompletion(true)? 0 : 1);
    }

}
