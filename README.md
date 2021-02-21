# Image comparison

The app is used to find the similar images using the `dHash` algorithm. To speed up the image processing, many operations are implemented concurrently.

## Processing scheme
![](images/processing-scheme.jpg)


## Supported endpoints
1. Downloads user's files, calculates the hash, saves images to the hard drive, creates a record in the persistent storage.
	```
	POST /image/batch
	content-type: multipart/form-data
	images: MultipartFile[]
	```

2. Downloads the file to be searched for in the persistent storage with the specified minimum match percentage (threshold). The threshold should be in the range (0, 1], if it is absent, 0.9 is used as the default value. If no similar images are found, then the image is saved to the hard drive and the entry about it is added to the persistent storage.
	```
	POST /image/search?threshold=?
	content-type: multipart/form-data
	image: MultipartFile

	Response: [
		{
			id: "92c73b0f-77d6-41b9-be87-1e0ebf20be31",
			image: "http://127.0.0.1:8080/files/92c73b0f-77d6-41b9-be87-1e0ebf20be31.jpg",
			match: 96.1265
		}
	]
	```

3. Removes the specified image from the persistent storage and the hard drive.
    ```
	DELETE /image/{id}
	```

4. Removes all images from the hard drive and the persistent storage.
	```
	DELETE /image/purge
	```