package io.github.interestinglab.waterdrop.input

import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.apis.BaseStreamingInput
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class S3Stream extends BaseStreamingInput {

  var config: Config = ConfigFactory.empty()

  /**
   * Set Config.
   * */
  override def setConfig(config: Config): Unit = {
    this.config = config
  }

  /**
   * Get Config.
   * */
  override def getConfig(): Config = {
    this.config
  }

  override def checkConfig(): (Boolean, String) = {
    config.hasPath("path") match {
      case true => {
        val allowedURISchema = List("s3://", "s3a://", "s3n://")
        val dir = config.getString("path")
        val unSupportedSchema = allowedURISchema.forall(schema => {
          // there are 3 "/" in dir, first 2 are from URI Schema, and the last is from path
          !dir.startsWith(schema + "/")
        })

        unSupportedSchema match {
          case true =>
            (false, "unsupported schema, please set the following allowed schemas: " + allowedURISchema.mkString(", "))
          case false => (true, "")
        }
      }
      case false => (false, "please specify [path] as non-empty string")
    }
  }

  override def getDStream(ssc: StreamingContext): DStream[(String, String)] = {

    ssc.textFileStream(config.getString("path")).map(s => { ("", s) })
  }
}
