import interactions
import aiohttp
import os

bot = interactions.Client(token=os.getenv("CLOUDCODE_DISCORD_TOKEN"))
http_session = aiohttp.ClientSession()

HOST = 'localhost'
API_KEY = os.getenv("CODECLOUD_API_KEY")


@bot.command(name="run", description="Start a new session to run some code",
             options=[
                 interactions.Option(
                     name="language",
                     description="Specify which language you want to run.",
                     type=interactions.OptionType.STRING,
                     required=True,
                     choices=[
                         interactions.Choice(name="Interactive python shell", value="IPYTHON"),
                         interactions.Choice(name="Python file", value="PYTHON"),
                         interactions.Choice(name="C file", value="C")
                     ]
                 ),
                interactions.Option(
                     name="file",
                     description="Specify which language you want to run.",
                     type=interactions.OptionType.ATTACHMENT,
                     required=False
                 )
             ])
async def run(ctx: interactions.CommandContext, language: str, file: interactions.Attachment=None):
    await ctx.defer()
    file_url = file.url if file is not None else ""
    async with http_session.post(f"http://{HOST}:8080/api/session/create",
                                 json={"user_id": str(ctx.user.id), "language": language, "file_url": file_url},
                                 headers={"Authorization": f"Bearer {API_KEY}"}) as resp:
        if resp.status != 200:
            await ctx.send(f"Error when creating session: {await resp.text()}")
        else:
            async with http_session.get(f"http://{HOST}:8080/api/session/output?userId={str(ctx.user.id)}",
                                        headers={"Authorization": f"Bearer {API_KEY}"}) as resp2:
                if resp2.status != 200:
                    await ctx.send(f"Error when getting output: {await resp2.text()}")
                else:
                    output = await resp2.text()
                    try:
                        await ctx.send(output)
                    except:
                        await ctx.send("*No additional output*")


@bot.command(name="send", description="Send input to your program",
             options=[
                 interactions.Option(
                     name="cmd_input",
                     description="Command line input",
                     type=interactions.OptionType.STRING,
                     required=True
                 )
             ])
async def send(ctx: interactions.CommandContext, cmd_input: str):
    await ctx.defer()
    input_str = list(cmd_input)
    formatted_input_str = []
    while ''.join(input_str[:5]) == "[TAB]":
        formatted_input_str.append(" " * 4)
        input_str = input_str[5:]
    formatted_input_str.append(''.join(input_str))

    async with http_session.post(f"http://{HOST}:8080/api/session/input",
                                 json={"user_id": str(ctx.user.id), "input": ''.join(formatted_input_str)},
                                 headers={"Authorization": f"Bearer {API_KEY}"}) as resp:
        if resp.status != 200:
            await ctx.send(f"Error when providing input: {await resp.text()}")
        else:
            async with http_session.get(f"http://{HOST}:8080/api/session/output?userId={str(ctx.user.id)}",
                                        headers={"Authorization": f"Bearer {API_KEY}"}) as resp2:
                if resp2.status != 200:
                    await ctx.send(f"Error when getting output: {await resp2.text()}")
                else:
                    output = await resp2.text()
                    try:
                        await ctx.send(output)
                    except:
                        await ctx.send("*No additional output*")


@bot.command(name="get", description="Get additional output from your program (if any)")
async def get(ctx: interactions.CommandContext):
    await ctx.defer()
    async with http_session.get(f"http://{HOST}:8080/api/session/output?userId={str(ctx.user.id)}",
                                headers={"Authorization": f"Bearer {API_KEY}"}) as resp:
        if resp.status != 200:
            await ctx.send(f"Error when getting output: {await resp.text()}")
        else:
            output = await resp.text()
            try:
                await ctx.send(output)
            except:
                await ctx.send("*No additional output*")


@bot.command(name="close", description="Forcibly shutdown your program")
async def close(ctx: interactions.CommandContext):
    await ctx.defer()
    async with http_session.post(f"http://{HOST}:8080/api/session/delete",
                                json={"user_id": str(ctx.user.id)},
                                headers={"Authorization": f"Bearer {API_KEY}"}) as resp:
        if resp.status != 200:
            await ctx.send(f"Error when shutting down program: {await resp.text()}")
        else:
            await ctx.send("Program sucesssfully shut down")

bot.start()
